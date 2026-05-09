package com.smartledger.controller;

import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@Tag(name = "数据导出")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/csv")
    @Operation(summary = "导出CSV")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        byte[] data = exportService.exportCsv(userId, bookId, startDate, endDate);
        String filename = "records_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }
}
