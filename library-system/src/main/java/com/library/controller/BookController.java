package com.library.controller;

import com.library.annotation.RequirePermission;
import com.library.dto.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import com.library.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@Tag(name = "图书管理", description = "图书增删改查及相关接口")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取所有图书列表")
    public Result<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有图书（含状态筛选）", description = "管理员查看所有图书，可按状态筛选")
    public Result<List<Book>> getAllBooksWithStatus(
            @Parameter(description = "图书状态") @RequestParam(required = false) Integer status) {
        try {
            List<Book> books = bookService.getAllBooksWithStatus(status);
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "根据ID获取图书详情")
    public Result<Book> getBookById(
            @Parameter(description = "图书ID") @PathVariable Integer id) {
        try {
            Book book = bookService.getBookById(id);
            return Result.success(book);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "搜索图书", description = "根据关键词搜索图书")
    public Result<List<Book>> searchBooks(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<Book> books = bookService.searchBooks(keyword);
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @RequirePermission("book:create")
    @Operation(summary = "新增图书")
    public Result<Book> addBook(@RequestBody Book book) {
        try {
            Book newBook = bookService.addBook(book);
            return Result.success(newBook);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @RequirePermission("book:update")
    @Operation(summary = "更新图书信息")
    public Result<Book> updateBook(
            @Parameter(description = "图书ID") @PathVariable Integer id,
            @RequestBody Book book) {
        try {
            book.setId(id);
            Book updatedBook = bookService.updateBook(book);
            return Result.success(updatedBook);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission("book:delete")
    @Operation(summary = "删除图书")
    public Result<Void> deleteBook(
            @Parameter(description = "图书ID") @PathVariable Integer id) {
        try {
            bookService.deleteBook(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取相似图书（用于图书详情页）
     */
    @GetMapping("/{id}/similar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取相似图书推荐", description = "根据图书ID获取相似图书列表")
    public Result<List<Map<String, Object>>> getSimilarBooks(
            @Parameter(description = "图书ID") @PathVariable Integer id,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> similarBooks = recommendationService.getSimilarBooks(id, limit);
            return Result.success(similarBooks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出图书列表
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @RequirePermission("book:export")
    @Operation(summary = "导出图书列表", description = "导出所有图书数据")
    public Result<List<Book>> exportBooks() {
        try {
            List<Book> books = bookService.exportBooks();
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导入图书
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission("book:import")
    @Operation(summary = "批量导入图书", description = "批量导入图书数据")
    public Result<Void> importBooks(@RequestBody List<Book> books) {
        try {
            bookService.importBooks(books);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
