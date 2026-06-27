package com.library.service.impl;

import com.library.entity.OperationLog;
import com.library.mapper.OperationLogMapper;
import com.library.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

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

    @Override
    public void saveLog(OperationLog log) {
        operationLogMapper.insert(log);
    }
}
