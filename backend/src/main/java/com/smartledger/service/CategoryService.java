package com.smartledger.service;

import com.smartledger.dto.request.CategoryRequest;
import com.smartledger.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getCategories(Long userId, Integer type, Boolean includeHidden);
    Category getCategory(Long id);
    Category createCategory(Long userId, CategoryRequest request);
    Category updateCategory(Long userId, Long id, CategoryRequest request);
    long getCategoryTransactionsCount(Long userId, Long categoryId);
    void deleteCategory(Long userId, Long id);
    void initSystemCategories();
}
