package com.smartledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartledger.entity.OperationLog;

public interface OperationLogService {
    void log(Long userId, String operationType, String targetType, Long targetId, String content, String ipAddress);
    Page<OperationLog> getLogs(Long userId, int page, int size);
}
