package com.library.service;

import com.library.entity.OperationLog;

import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    List<OperationLog> getAllLogs();
    List<OperationLog> getLogsByUserId(Integer userId);
    List<OperationLog> getLogsByModule(String module);
    void saveLog(OperationLog log);
}
