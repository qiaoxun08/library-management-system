package com.library.dto;

public class BookCategoryCount {
    private String category;
    private Integer count;

    public BookCategoryCount() {}

    public BookCategoryCount(String category, Integer count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
