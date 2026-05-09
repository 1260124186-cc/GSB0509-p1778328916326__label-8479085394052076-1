package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.dto.request.TagRequest;
import com.smartledger.entity.Tag;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.TagMapper;
import com.smartledger.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Tag> getTags(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId).orderByDesc(Tag::getCreatedAt);
        return tagMapper.selectList(wrapper);
    }

    @Override
    public Tag createTag(Long userId, TagRequest request) {
        // 检查标签名称是否与已存在的标签重复
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getUserId, userId)
                   .eq(Tag::getName, request.getName());

        Long count = tagMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Tag updateTag(Long userId, Long id, TagRequest request) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new BusinessException(404, "标签不存在");
        }

        // 检查标签名称是否与其他标签重复（排除当前标签）
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getUserId, userId)
                   .eq(Tag::getName, request.getName())
                   .ne(Tag::getId, id);

        Long count = tagMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tagMapper.updateById(tag);
        return tag;
    }

    @Override
    public void deleteTag(Long userId, Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new BusinessException(404, "标签不存在");
        }
        tagMapper.deleteById(id);
    }

    @Override
    public List<Tag> getTagsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return tagMapper.selectBatchIds(ids);
    }
}
