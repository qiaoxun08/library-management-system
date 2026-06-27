package com.library.service;

import com.library.entity.Borrowing;
import com.library.dto.BorrowingDTO;
import java.util.List;

public interface BorrowingService {
    List<BorrowingDTO> getAllBorrowings();
    Borrowing getBorrowingById(Integer id);
    List<Borrowing> getBorrowingsByReaderId(Integer readerId);
    List<Borrowing> getBorrowingsByReaderIdString(String readerId);
    Borrowing borrowBook(String readerId, Integer bookId);
    Borrowing returnBook(Integer borrowingId);
    Borrowing renewBook(Integer borrowingId);
    void payFine(Integer borrowingId);
    void validateOwnership(Integer borrowingId, String readerId);
}
