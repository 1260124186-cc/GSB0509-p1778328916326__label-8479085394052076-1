package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.entity.Category;
import com.smartledger.entity.Transaction;
import com.smartledger.mapper.CategoryMapper;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public byte[] exportCsv(Long userId, Long bookId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId);
        if (bookId != null) {
            wrapper.eq(Transaction::getBookId, bookId);
        }
        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        wrapper.orderByDesc(Transaction::getTransactionDate);
        List<Transaction> transactions = transactionMapper.selectList(wrapper);

        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.write("\uFEFF");
            writer.println("日期,类型,分类,金额,备注,支付方式");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Transaction t : transactions) {
                String date = t.getTransactionDate().format(formatter);
                String type = t.getType() == 1 ? "收入" : "支出";
                String category = categoryMap.getOrDefault(t.getCategoryId(), "未知");
                String amount = t.getAmount().toPlainString();
                String remark = t.getRemark() != null ? t.getRemark().replace(",", "，") : "";
                String paymentMethod = t.getPaymentMethod() != null ? t.getPaymentMethod() : "";

                writer.println(String.format("%s,%s,%s,%s,%s,%s", date, type, category, amount, remark, paymentMethod));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出CSV失败", e);
        }
    }
}
