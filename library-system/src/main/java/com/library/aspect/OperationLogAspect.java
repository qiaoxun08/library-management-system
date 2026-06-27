package com.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.annotation.OperLog;
import com.library.entity.OperationLog;
import com.library.service.OperationLogService;
import com.library.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.library.annotation.OperLog)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long costTime = System.currentTimeMillis() - startTime;

        try {
            recordLog(joinPoint, costTime);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }

        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long costTime) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        String username = "未知";
        String role = "未知";
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(token);
                role = jwtUtil.getRoleFromToken(token);
            } catch (Exception e) {
                log.warn("解析JWT token失败", e);
            }
        }

        // 构建详细的操作信息
        String detail = buildDetail(joinPoint, signature, costTime);

        OperationLog logEntity = new OperationLog();
        logEntity.setUsername(username);
        logEntity.setRole(role);
        logEntity.setModule(operLog.module());
        logEntity.setAction(operLog.action());
        logEntity.setDetail(detail);
        logEntity.setIp(getClientIp(request));

        operationLogService.saveLog(logEntity);
    }

    private String buildDetail(ProceedingJoinPoint joinPoint, MethodSignature signature, long costTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("耗时: ").append(costTime).append("ms");

        // 记录方法参数
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        if (args != null && args.length > 0) {
            sb.append("; 参数: ");
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) continue;
                // 跳过 request/response 等大对象
                String className = args[i].getClass().getSimpleName();
                if (className.contains("Request") || className.contains("Response")
                        || className.contains("Session") || className.contains("InputStream")) {
                    continue;
                }
                if (i > 0) sb.append(", ");
                sb.append(paramNames[i]).append("=");
                try {
                    String json = objectMapper.writeValueAsString(args[i]);
                    // 截断过长的内容
                    if (json.length() > 200) {
                        json = json.substring(0, 200) + "...";
                    }
                    sb.append(json);
                } catch (Exception e) {
                    sb.append(args[i].toString());
                }
            }
        }

        return sb.toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
