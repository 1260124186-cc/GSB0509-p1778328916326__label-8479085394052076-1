package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.dto.request.BookRequest;
import com.smartledger.dto.response.BookResponse;
import com.smartledger.entity.Book;
import com.smartledger.entity.Transaction;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.BookMapper;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.service.BookService;
import com.smartledger.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<BookResponse> getBooks(Long userId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId).orderByDesc(Book::getIsDefault).orderByAsc(Book::getCreatedAt);
        List<Book> books = bookMapper.selectList(wrapper);
        return books.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public BookResponse getBook(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException(404, "账本不存在");
        }
        return toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse createBook(Long userId, BookRequest request) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId).eq(Book::getName, request.getName());
        if (bookMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "账本名称已存在");
        }

        Book book = new Book();
        book.setUserId(userId);
        book.setName(request.getName());
        book.setIcon(request.getIcon());
        book.setDescription(request.getDescription());
        book.setIsDefault(request.getIsDefault() ? 1 : 0);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        if (request.getIsDefault()) {
            clearDefaultBook(userId);
        }

        bookMapper.insert(book);
        return toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long userId, Long bookId, BookRequest request) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException(404, "账本不存在");
        }

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId).eq(Book::getName, request.getName()).ne(Book::getId, bookId);
        if (bookMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "账本名称已存在");
        }

        // 记录修改账本日志
        String logContent = String.format("{\"name\":\"%s\"}", request.getName());
        String ipAddress = getClientIp();
        operationLogService.log(userId, "UPDATE_BOOK", "BOOK", bookId, logContent, ipAddress);

        book.setName(request.getName());
        book.setIcon(request.getIcon());
        book.setDescription(request.getDescription());
        book.setUpdatedAt(LocalDateTime.now());

        if (request.getIsDefault()) {
            clearDefaultBook(userId);
            book.setIsDefault(1);
        }

        bookMapper.updateById(book);
        return toResponse(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long userId, Long bookId) {
        LambdaQueryWrapper<Book> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Book::getUserId, userId);
        if (bookMapper.selectCount(countWrapper) <= 1) {
            throw new BusinessException(400, "至少保留一个账本");
        }

        Book book = bookMapper.selectById(bookId);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException(404, "账本不存在");
        }

        // 记录删除账本日志
        LambdaQueryWrapper<Transaction> transWrapper = new LambdaQueryWrapper<>();
        transWrapper.eq(Transaction::getBookId, bookId);
        long recordCount = transactionMapper.selectCount(transWrapper);
        String logContent = String.format("{\"name\":\"%s\",\"recordCount\":%d}", book.getName(), recordCount);
        String ipAddress = getClientIp();
        operationLogService.log(userId, "DELETE_BOOK", "BOOK", bookId, logContent, ipAddress);

        transactionMapper.delete(transWrapper);
        bookMapper.deleteById(bookId);

        if (book.getIsDefault() == 1) {
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Book::getUserId, userId).orderByAsc(Book::getCreatedAt).last("LIMIT 1");
            Book firstBook = bookMapper.selectOne(wrapper);
            if (firstBook != null) {
                firstBook.setIsDefault(1);
                bookMapper.updateById(firstBook);
            }
        }
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
    public BookResponse getDefaultBook(Long userId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId).eq(Book::getIsDefault, 1);
        Book book = bookMapper.selectOne(wrapper);
        if (book == null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Book::getUserId, userId).orderByAsc(Book::getCreatedAt).last("LIMIT 1");
            book = bookMapper.selectOne(wrapper);
        }
        return book != null ? toResponse(book) : null;
    }

    private void clearDefaultBook(Long userId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId).eq(Book::getIsDefault, 1);
        Book defaultBook = bookMapper.selectOne(wrapper);
        if (defaultBook != null) {
            defaultBook.setIsDefault(0);
            bookMapper.updateById(defaultBook);
        }
    }

    private BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();
        BeanUtils.copyProperties(book, response);
        response.setIsDefault(book.getIsDefault() == 1);

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, book.getId());
        response.setRecordCount(transactionMapper.selectCount(wrapper));

        return response;
    }
}
