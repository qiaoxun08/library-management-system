package com.library.dto;

import com.library.entity.Borrowing;

public class BorrowingDTO extends Borrowing {
    private String bookTitle;
    private String bookAuthor;
    private String readerName;

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }
    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }
}
