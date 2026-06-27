package com.library.service.impl;

import com.library.dto.BorrowingDTO;
import com.library.entity.OperationLog;
import com.library.entity.ReaderLevel;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.OperationLogMapper;
import com.library.mapper.ReaderLevelMapper;
import com.library.service.ExportService;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务实现
 * 使用 SXSSFWorkbook 流式写入，支持大数据量导出
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private ReaderLevelMapper readerLevelMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void exportBorrowings(String startDate, String endDate, String format, HttpServletResponse response) throws Exception {
        List<BorrowingDTO> borrowings = borrowingMapper.findAllWithBook();

        // 按日期筛选
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            borrowings = borrowings.stream()
                    .filter(b -> b.getBorrowDate() != null && !b.getBorrowDate().toLocalDate().isBefore(start))
                    .toList();
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            borrowings = borrowings.stream()
                    .filter(b -> b.getBorrowDate() != null && !b.getBorrowDate().toLocalDate().isAfter(end))
                    .toList();
        }

        String filename = "borrowings_" + System.currentTimeMillis();
        if ("csv".equalsIgnoreCase(format)) {
            writeBorrowingsCsv(borrowings, filename, response);
        } else {
            writeBorrowingsExcel(borrowings, filename, response);
        }
    }

    @Override
    public void exportOverdue(String month, String format, HttpServletResponse response) throws Exception {
        List<BorrowingDTO> all = borrowingMapper.findAllWithBook();

        // 筛选逾期且在指定月份的记录
        List<BorrowingDTO> overdue;
        if (month != null && !month.isEmpty()) {
            String prefix = month; // yyyy-MM
            overdue = all.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus() == 1
                            && b.getDueDate() != null
                            && b.getDueDate().isBefore(LocalDateTime.now())
                            && b.getBorrowDate() != null
                            && b.getBorrowDate().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(prefix))
                    .toList();
        } else {
            overdue = all.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus() == 1
                            && b.getDueDate() != null
                            && b.getDueDate().isBefore(LocalDateTime.now()))
                    .toList();
        }

        String filename = "overdue_" + (month != null ? month : "all") + "_" + System.currentTimeMillis();
        if ("csv".equalsIgnoreCase(format)) {
            writeBorrowingsCsv(overdue, filename, response);
        } else {
            writeBorrowingsExcel(overdue, filename, response);
        }
    }

    @Override
    public void exportReaderRanking(String format, HttpServletResponse response) throws Exception {
        List<ReaderLevel> levels = readerLevelMapper.findAll();

        String filename = "reader_ranking_" + System.currentTimeMillis();
        if ("csv".equalsIgnoreCase(format)) {
            writeRankingCsv(levels, filename, response);
        } else {
            writeRankingExcel(levels, filename, response);
        }
    }

    @Override
    public void exportOperationLogs(String format, HttpServletResponse response) throws Exception {
        List<OperationLog> logs = operationLogMapper.findAll();

        String filename = "operation_logs_" + System.currentTimeMillis();
        if ("csv".equalsIgnoreCase(format)) {
            writeLogsCsv(logs, filename, response);
        } else {
            writeLogsExcel(logs, filename, response);
        }
    }

    // ==================== Excel 写入 ====================

    private void writeBorrowingsExcel(List<BorrowingDTO> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("借阅记录");
            String[] headers = {"ID", "读者", "图书", "借阅日期", "应还日期", "归还日期", "状态", "续借次数", "罚款"};
            writeHeader(sheet, headers);

            int rowIdx = 1;
            for (BorrowingDTO b : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId() != null ? b.getId() : 0);
                row.createCell(1).setCellValue(b.getReaderName() != null ? b.getReaderName() : "");
                row.createCell(2).setCellValue(b.getBookTitle() != null ? b.getBookTitle() : "");
                row.createCell(3).setCellValue(formatDateTime(b.getBorrowDate()));
                row.createCell(4).setCellValue(formatDateTime(b.getDueDate()));
                row.createCell(5).setCellValue(formatDateTime(b.getReturnDate()));
                row.createCell(6).setCellValue(borrowStatusText(b.getStatus()));
                row.createCell(7).setCellValue(b.getRenewCount() != null ? b.getRenewCount() : 0);
                row.createCell(8).setCellValue(b.getFineAmount() != null ? b.getFineAmount().doubleValue() : 0);
            }
            workbook.write(out);
        }
    }

    private void writeRankingExcel(List<ReaderLevel> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("读者积分排行");
            String[] headers = {"排名", "读者ID", "积分", "等级", "累计借阅", "按时归还"};
            writeHeader(sheet, headers);

            // 按积分降序
            data.sort((a, b) -> Integer.compare(
                    b.getPoints() != null ? b.getPoints() : 0,
                    a.getPoints() != null ? a.getPoints() : 0));

            int rank = 1;
            int rowIdx = 1;
            for (ReaderLevel rl : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(rl.getReaderId() != null ? rl.getReaderId() : 0);
                row.createCell(2).setCellValue(rl.getPoints() != null ? rl.getPoints() : 0);
                row.createCell(3).setCellValue(rl.getLevel() != null ? rl.getLevel() : "");
                row.createCell(4).setCellValue(rl.getTotalBorrowCount() != null ? rl.getTotalBorrowCount() : 0);
                row.createCell(5).setCellValue(rl.getTotalReturnOnTime() != null ? rl.getTotalReturnOnTime() : 0);
            }
            workbook.write(out);
        }
    }

    private void writeLogsExcel(List<OperationLog> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("操作日志");
            String[] headers = {"ID", "用户", "角色", "模块", "操作", "详情", "IP", "时间"};
            writeHeader(sheet, headers);

            int rowIdx = 1;
            for (OperationLog log : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getId() != null ? log.getId() : 0);
                row.createCell(1).setCellValue(log.getUsername() != null ? log.getUsername() : "");
                row.createCell(2).setCellValue(log.getRole() != null ? log.getRole() : "");
                row.createCell(3).setCellValue(log.getModule() != null ? log.getModule() : "");
                row.createCell(4).setCellValue(log.getAction() != null ? log.getAction() : "");
                row.createCell(5).setCellValue(log.getDetail() != null ? log.getDetail() : "");
                row.createCell(6).setCellValue(log.getIp() != null ? log.getIp() : "");
                row.createCell(7).setCellValue(formatDateTime(log.getCreateTime()));
            }
            workbook.write(out);
        }
    }

    // ==================== CSV 写入 ====================

    private void writeBorrowingsCsv(List<BorrowingDTO> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".csv", StandardCharsets.UTF_8));
        response.setCharacterEncoding("UTF-8");

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"ID", "读者", "图书", "借阅日期", "应还日期", "归还日期", "状态", "续借次数", "罚款"});
            for (BorrowingDTO b : data) {
                writer.writeNext(new String[]{
                        String.valueOf(b.getId()),
                        b.getReaderName() != null ? b.getReaderName() : "",
                        b.getBookTitle() != null ? b.getBookTitle() : "",
                        formatDateTime(b.getBorrowDate()),
                        formatDateTime(b.getDueDate()),
                        formatDateTime(b.getReturnDate()),
                        borrowStatusText(b.getStatus()),
                        String.valueOf(b.getRenewCount() != null ? b.getRenewCount() : 0),
                        b.getFineAmount() != null ? b.getFineAmount().toString() : "0"
                });
            }
        }
    }

    private void writeRankingCsv(List<ReaderLevel> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".csv", StandardCharsets.UTF_8));

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"排名", "读者ID", "积分", "等级", "累计借阅", "按时归还"});
            data.sort((a, b) -> Integer.compare(
                    b.getPoints() != null ? b.getPoints() : 0,
                    a.getPoints() != null ? a.getPoints() : 0));
            int rank = 1;
            for (ReaderLevel rl : data) {
                writer.writeNext(new String[]{
                        String.valueOf(rank++),
                        String.valueOf(rl.getReaderId()),
                        String.valueOf(rl.getPoints() != null ? rl.getPoints() : 0),
                        rl.getLevel() != null ? rl.getLevel() : "",
                        String.valueOf(rl.getTotalBorrowCount() != null ? rl.getTotalBorrowCount() : 0),
                        String.valueOf(rl.getTotalReturnOnTime() != null ? rl.getTotalReturnOnTime() : 0)
                });
            }
        }
    }

    private void writeLogsCsv(List<OperationLog> data, String filename, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".csv", StandardCharsets.UTF_8));

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"ID", "用户", "角色", "模块", "操作", "详情", "IP", "时间"});
            for (OperationLog log : data) {
                writer.writeNext(new String[]{
                        String.valueOf(log.getId()),
                        log.getUsername() != null ? log.getUsername() : "",
                        log.getRole() != null ? log.getRole() : "",
                        log.getModule() != null ? log.getModule() : "",
                        log.getAction() != null ? log.getAction() : "",
                        log.getDetail() != null ? log.getDetail() : "",
                        log.getIp() != null ? log.getIp() : "",
                        formatDateTime(log.getCreateTime())
                });
            }
        }
    }

    // ==================== 工具方法 ====================

    private void writeHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String borrowStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "借阅中";
            case 2 -> "已归还";
            case 3 -> "已取消";
            case 4 -> "已过期";
            default -> "未知";
        };
    }
}
