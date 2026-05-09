package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.BookRequest;
import com.smartledger.dto.response.BookResponse;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "账本管理")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @Operation(summary = "获取账本列表")
    public Result<List<BookResponse>> getBooks() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(bookService.getBooks(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取账本详情")
    public Result<BookResponse> getBook(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(bookService.getBook(userId, id));
    }

    @GetMapping("/default")
    @Operation(summary = "获取默认账本")
    public Result<BookResponse> getDefaultBook() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(bookService.getDefaultBook(userId));
    }

    @PostMapping
    @Operation(summary = "创建账本")
    public Result<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(bookService.createBook(userId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新账本")
    public Result<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(bookService.updateBook(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除账本")
    public Result<Void> deleteBook(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        bookService.deleteBook(userId, id);
        return Result.success();
    }
}
