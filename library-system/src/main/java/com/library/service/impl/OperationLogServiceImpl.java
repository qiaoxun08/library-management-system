package com.library.service.impl;

import com.library.entity.OperationLog;
import com.library.mapper.OperationLogMapper;
import com.library.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLog> getAllLogs() {
        return operationLogMapper.findAll();
    }

    @Override
    public List<OperationLog> getLogsByUserId(Integer userId) {
        return operationLogMapper.findByUserId(userId);
    }

    @Override
    public List<OperationLog> getLogsByModule(String module) {
        return operationLogMapper.findByModule(module);
    }

    /**
     * 异步保存操作日志
     * 使用 @Async 注解，日志写入不阻塞主业务流程
     */
    @Override
    @Async("logExecutor")
    public void saveLog(OperationLog logEntry) {
        try {
            operationLogMapper.insert(logEntry);
            log.debug("异步日志写入成功: module={}, action={}", logEntry.getModule(), logEntry.getAction());
        } catch (Exception e) {
            // 日志写入失败不应影响主业务，记录错误但不抛出
            log.error("异步日志写入失败: module={}, action={}", logEntry.getModule(), logEntry.getAction(), e);
        }
    }
}
