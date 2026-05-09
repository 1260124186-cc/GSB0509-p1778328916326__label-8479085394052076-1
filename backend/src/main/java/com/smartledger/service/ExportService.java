package com.smartledger.service;

import com.smartledger.entity.Transaction;
import java.time.LocalDate;
import java.util.List;

public interface ExportService {
    byte[] exportCsv(Long userId, Long bookId, LocalDate startDate, LocalDate endDate);
}
