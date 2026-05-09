package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartledger.dto.request.TransactionRequest;
import com.smartledger.dto.response.TransactionResponse;
import com.smartledger.entity.Category;
import com.smartledger.entity.Tag;
import com.smartledger.entity.Transaction;
import com.smartledger.entity.TransactionTag;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.CategoryMapper;
import com.smartledger.mapper.TagMapper;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.mapper.TransactionTagMapper;
import com.smartledger.service.OperationLogService;
import com.smartledger.service.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionTagMapper transactionTagMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<TransactionResponse> getTransactions(Long userId, Long bookId, Integer type, Long categoryId,
                                                     LocalDate startDate, LocalDate endDate, String keyword, Long tagId,
                                                     int page, int size) {
        // 修复BUG-007：如果指定了标签筛选，需要在数据库层面处理，而不是内存过滤
        if (tagId != null) {
            return getTransactionsByTag(userId, bookId, type, categoryId, startDate, endDate, keyword, tagId, page, size);
        }

        Page<Transaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId);

        if (bookId != null) {
            wrapper.eq(Transaction::getBookId, bookId);
        }
        if (type != null) {
            wrapper.eq(Transaction::getType, type);
        }
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }
        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Transaction::getRemark, keyword);
        }
        wrapper.orderByDesc(Transaction::getTransactionDate).orderByDesc(Transaction::getCreatedAt);

        Page<Transaction> result = transactionMapper.selectPage(pageParam, wrapper);

        Page<TransactionResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    /**
     * 按标签筛选记录（修复BUG-007）
     * 使用子查询在数据库层面完成筛选，确保total字段准确
     */
    private Page<TransactionResponse> getTransactionsByTag(Long userId, Long bookId, Integer type, Long categoryId,
                                                           LocalDate startDate, LocalDate endDate, String keyword, Long tagId,
                                                           int page, int size) {
        // 查询带有指定标签的交易ID
        LambdaQueryWrapper<TransactionTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(TransactionTag::getTagId, tagId);
        List<TransactionTag> transactionTags = transactionTagMapper.selectList(tagWrapper);
        List<Long> transactionIds = transactionTags.stream()
                .map(TransactionTag::getTransactionId)
                .collect(Collectors.toList());

        if (transactionIds.isEmpty()) {
            // 没有符合条件的记录，返回空结果
            Page<TransactionResponse> emptyPage = new Page<>(page, size, 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 使用这些ID查询交易记录
        Page<Transaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId);
        wrapper.in(Transaction::getId, transactionIds);

        if (bookId != null) {
            wrapper.eq(Transaction::getBookId, bookId);
        }
        if (type != null) {
            wrapper.eq(Transaction::getType, type);
        }
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }
        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Transaction::getRemark, keyword);
        }
        wrapper.orderByDesc(Transaction::getTransactionDate).orderByDesc(Transaction::getCreatedAt);

        Page<Transaction> result = transactionMapper.selectPage(pageParam, wrapper);

        Page<TransactionResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    public TransactionResponse getTransaction(Long userId, Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || !transaction.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setBookId(request.getBookId());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setRemark(request.getRemark());
        transaction.setPaymentMethod(request.getPaymentMethod());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                transaction.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (Exception e) {
                transaction.setImages("[]");
            }
        }

        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                TransactionTag tt = new TransactionTag();
                tt.setTransactionId(transaction.getId());
                tt.setTagId(tagId);
                transactionTagMapper.insert(tt);
            }
        }

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || !transaction.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }

        // 检查是否需要记录日志（金额变化或大额记录）
        boolean needLog = false;
        if (transaction.getAmount().compareTo(request.getAmount()) != 0) {
            needLog = true;
        }
        // 金额大于1000时记录日志
        if (request.getAmount().compareTo(new java.math.BigDecimal("1000")) > 0) {
            needLog = true;
        }

        if (needLog) {
            String logContent = String.format("{\"oldAmount\":\"%s\",\"newAmount\":\"%s\",\"remark\":\"%s\"}",
                transaction.getAmount(), request.getAmount(), request.getRemark());
            String ipAddress = getClientIp();
            operationLogService.log(userId, "UPDATE_TRANSACTION", "TRANSACTION", id, logContent, ipAddress);
        }

        transaction.setBookId(request.getBookId());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setRemark(request.getRemark());
        transaction.setPaymentMethod(request.getPaymentMethod());

        if (request.getImages() != null) {
            try {
                transaction.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (Exception e) {
                transaction.setImages("[]");
            }
        }

        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.updateById(transaction);

        transactionTagMapper.deleteByTransactionId(id);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                TransactionTag tt = new TransactionTag();
                tt.setTransactionId(id);
                tt.setTagId(tagId);
                transactionTagMapper.insert(tt);
            }
        }

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || !transaction.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }

        // 记录删除日志
        String logContent = String.format("{\"amount\":\"%s\",\"remark\":\"%s\"}",
            transaction.getAmount(), transaction.getRemark());
        String ipAddress = getClientIp();
        operationLogService.log(userId, "DELETE_TRANSACTION", "TRANSACTION", id, logContent, ipAddress);

        transactionTagMapper.deleteByTransactionId(id);
        transactionMapper.deleteById(id);
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    @Transactional
    public void batchDelete(Long userId, List<Long> ids) {
        for (Long id : ids) {
            deleteTransaction(userId, id);
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setBookId(transaction.getBookId());
        response.setCategoryId(transaction.getCategoryId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setRemark(transaction.getRemark());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());

        Category category = categoryMapper.selectById(transaction.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
            response.setCategoryIcon(category.getIcon());
        }

        if (StringUtils.isNotBlank(transaction.getImages())) {
            try {
                response.setImages(objectMapper.readValue(transaction.getImages(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                response.setImages(new ArrayList<>());
            }
        } else {
            response.setImages(new ArrayList<>());
        }

        LambdaQueryWrapper<TransactionTag> ttWrapper = new LambdaQueryWrapper<>();
        ttWrapper.eq(TransactionTag::getTransactionId, transaction.getId());
        List<TransactionTag> tts = transactionTagMapper.selectList(ttWrapper);
        if (!tts.isEmpty()) {
            List<Long> tagIds = tts.stream().map(TransactionTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            response.setTags(tags.stream().map(tag -> {
                TransactionResponse.TagInfo tagInfo = new TransactionResponse.TagInfo();
                tagInfo.setId(tag.getId());
                tagInfo.setName(tag.getName());
                tagInfo.setColor(tag.getColor());
                return tagInfo;
            }).collect(Collectors.toList()));
        } else {
            response.setTags(new ArrayList<>());
        }

        return response;
    }
}
