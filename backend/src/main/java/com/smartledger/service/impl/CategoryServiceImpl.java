package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.dto.request.CategoryRequest;
import com.smartledger.entity.Category;
import com.smartledger.entity.Transaction;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.CategoryMapper;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public List<Category> getCategories(Long userId, Integer type, Boolean includeHidden) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Category::getUserId, 0L).or().eq(Category::getUserId, userId))
               .orderByAsc(Category::getIsHidden)
               .orderByAsc(Category::getSortOrder);
        // 只在不需要包含隐藏分类时过滤
        if (includeHidden == null || !includeHidden) {
            wrapper.eq(Category::getIsHidden, 0);
        }
        if (type != null) {
            wrapper.eq(Category::getType, type);
        }
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public Category getCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Category createCategory(Long userId, CategoryRequest request) {
        // 检查分类名称是否与已存在的分类重复（包括系统分类和其他自定义分类）
        LambdaQueryWrapper<Category> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.and(w -> w.eq(Category::getUserId, 0L).or().eq(Category::getUserId, userId))
                   .eq(Category::getName, request.getName())
                   .eq(Category::getType, request.getType())
                   .eq(Category::getIsHidden, 0);

        Long count = categoryMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setType(request.getType());
        category.setIsSystem(0);
        category.setSortOrder(request.getSortOrder());
        category.setIsHidden(0);
        category.setCreatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Long userId, Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (category.getIsSystem() == 1) {
            // 系统分类只允许修改isHidden字段
            if (request.getIsHidden() != null && !request.getIsHidden().equals(category.getIsHidden())) {
                category.setIsHidden(request.getIsHidden());
                categoryMapper.updateById(category);
                return category;
            }
            throw new BusinessException(400, "系统分类不可修改");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此分类");
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder());
        if (request.getIsHidden() != null) {
            category.setIsHidden(request.getIsHidden());
        }
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    public long getCategoryTransactionsCount(Long userId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问此分类");
        }

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
               .eq(Transaction::getCategoryId, categoryId);
        return transactionMapper.selectCount(wrapper);
    }

    @Override
    public void deleteCategory(Long userId, Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (category.getIsSystem() == 1) {
            throw new BusinessException(400, "系统分类不可删除");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此分类");
        }

        // 检查是否有关联的记录
        long count = getCategoryTransactionsCount(userId, id);
        if (count > 0) {
            throw new BusinessException(400, "该分类下有 " + count + " 条记录，请先迁移记录到其他分类");
        }

        categoryMapper.deleteById(id);
    }

    @Override
    @PostConstruct
    public void initSystemCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsSystem, 1);
        if (categoryMapper.selectCount(wrapper) > 0) {
            return;
        }

        List<String[]> expenseCategories = Arrays.asList(
            new String[]{"餐饮", "food"},
            new String[]{"交通", "car"},
            new String[]{"购物", "shopping-cart"},
            new String[]{"娱乐", "film"},
            new String[]{"居家", "home"},
            new String[]{"医疗", "first-aid"},
            new String[]{"教育", "book"},
            new String[]{"通讯", "phone"},
            new String[]{"人情", "gift"},
            new String[]{"其他", "ellipsis"}
        );

        List<String[]> incomeCategories = Arrays.asList(
            new String[]{"工资", "money"},
            new String[]{"奖金", "trophy"},
            new String[]{"投资", "chart-line"},
            new String[]{"兼职", "briefcase"},
            new String[]{"红包", "red-envelope"},
            new String[]{"其他", "ellipsis"}
        );

        int order = 1;
        for (String[] cat : expenseCategories) {
            Category category = new Category();
            category.setUserId(0L);
            category.setName(cat[0]);
            category.setIcon(cat[1]);
            category.setType(2);
            category.setIsSystem(1);
            category.setSortOrder(order++);
            category.setIsHidden(0);
            category.setCreatedAt(LocalDateTime.now());
            categoryMapper.insert(category);
        }

        order = 1;
        for (String[] cat : incomeCategories) {
            Category category = new Category();
            category.setUserId(0L);
            category.setName(cat[0]);
            category.setIcon(cat[1]);
            category.setType(1);
            category.setIsSystem(1);
            category.setSortOrder(order++);
            category.setIsHidden(0);
            category.setCreatedAt(LocalDateTime.now());
            categoryMapper.insert(category);
        }
    }
}
