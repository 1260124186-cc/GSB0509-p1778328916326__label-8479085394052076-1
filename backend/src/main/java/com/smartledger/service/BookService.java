package com.smartledger.service;

import com.smartledger.dto.request.BookRequest;
import com.smartledger.dto.response.BookResponse;
import java.util.List;

public interface BookService {
    List<BookResponse> getBooks(Long userId);
    BookResponse getBook(Long userId, Long bookId);
    BookResponse createBook(Long userId, BookRequest request);
    BookResponse updateBook(Long userId, Long bookId, BookRequest request);
    void deleteBook(Long userId, Long bookId);
    BookResponse getDefaultBook(Long userId);
}
