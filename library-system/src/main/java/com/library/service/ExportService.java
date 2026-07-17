package com.library.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 数据导出服务接口
 */
public interface ExportService {

    /**
     * 导出借阅记录
     * @param startDate 开始日期（yyyy-MM-dd），可为null
     * @param endDate 结束日期（yyyy-MM-dd），可为null
     * @param format 格式：xlsx / csv
     */
    void exportBorrowings(String startDate, String endDate, String format, HttpServletResponse response) throws Exception;

    /**
     * 导出逾期记录
     * @param month 月份（yyyy-MM），可为null则当月
     * @param format 格式：xlsx / csv
     */
    void exportOverdue(String month, String format, HttpServletResponse response) throws Exception;

    /**
     * 导出读者积分排行
     * @param format 格式：xlsx / csv
     */
    void exportReaderRanking(String format, HttpServletResponse response) throws Exception;

    /**
     * 导出操作日志
     * @param format 格式：xlsx / csv
     */
    void exportOperationLogs(String format, HttpServletResponse response) throws Exception;

    /**
     * 导出我的借阅记录（读者端）
     * @param readerId 读者学号
     * @param format 格式：xlsx / csv
     */
    void exportMyBorrowings(String readerId, String format, HttpServletResponse response) throws Exception;
}
