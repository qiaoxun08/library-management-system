package com.library.dto;

import com.library.entity.Borrowing;

public class BorrowingDTO extends Borrowing {
    private String bookTitle;
    private String readerName;

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }
}
