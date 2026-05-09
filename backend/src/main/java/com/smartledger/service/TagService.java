package com.smartledger.service;

import com.smartledger.dto.request.TagRequest;
import com.smartledger.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> getTags(Long userId);
    Tag createTag(Long userId, TagRequest request);
    Tag updateTag(Long userId, Long id, TagRequest request);
    void deleteTag(Long userId, Long id);
    List<Tag> getTagsByIds(List<Long> ids);
}
