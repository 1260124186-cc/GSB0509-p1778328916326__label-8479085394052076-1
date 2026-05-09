package com.smartledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartledger.common.PageResult;
import com.smartledger.common.Result;
import com.smartledger.entity.OperationLog;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "操作日志")
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    @Operation(summary = "获取操作日志")
    public Result<PageResult<OperationLog>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Page<OperationLog> result = operationLogService.getLogs(userId, page, size);
        PageResult<OperationLog> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }
}
