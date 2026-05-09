package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.CategoryRequest;
import com.smartledger.entity.Category;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<List<Category>> getCategories(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false, defaultValue = "false") Boolean includeHidden) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(categoryService.getCategories(userId, type, includeHidden));
    }

    @PostMapping
    @Operation(summary = "创建分类")
    public Result<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(categoryService.createCategory(userId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(categoryService.updateCategory(userId, id, request));
    }

    @GetMapping("/{id}/transactions-count")
    @Operation(summary = "获取分类关联的记录数量")
    public Result<Long> getCategoryTransactionsCount(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(categoryService.getCategoryTransactionsCount(userId, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        categoryService.deleteCategory(userId, id);
        return Result.success();
    }
}
