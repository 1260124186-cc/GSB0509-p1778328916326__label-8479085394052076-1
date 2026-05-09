package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartledger.entity.OperationLog;
import com.smartledger.mapper.OperationLogMapper;
import com.smartledger.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void log(Long userId, String operationType, String targetType, Long targetId, String content, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setContent(content);
        log.setIpAddress(ipAddress);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Override
    public Page<OperationLog> getLogs(Long userId, int page, int size) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getUserId, userId).orderByDesc(OperationLog::getCreatedAt);
        return operationLogMapper.selectPage(pageParam, wrapper);
    }
}
