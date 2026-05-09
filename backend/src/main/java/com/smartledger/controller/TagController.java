package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.TagRequest;
import com.smartledger.entity.Tag;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    @Operation(summary = "获取标签列表")
    public Result<List<Tag>> getTags() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(tagService.getTags(userId));
    }

    @PostMapping
    @Operation(summary = "创建标签")
    public Result<Tag> createTag(@Valid @RequestBody TagRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(tagService.createTag(userId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<Tag> updateTag(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(tagService.updateTag(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public Result<Void> deleteTag(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        tagService.deleteTag(userId, id);
        return Result.success();
    }
}
