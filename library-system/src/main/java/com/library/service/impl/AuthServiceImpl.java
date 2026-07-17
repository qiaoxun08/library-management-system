package com.library.service.impl;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.Admin;
import com.library.entity.Librarian;
import com.library.entity.LoginLog;
import com.library.entity.Reader;
import com.library.mapper.AdminMapper;
import com.library.mapper.LibrarianMapper;
import com.library.mapper.LoginLogMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.AuthService;
import com.library.service.SystemConfigService;
import com.library.exception.BusinessException;
import com.library.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private LibrarianMapper librarianMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private HttpServletRequest request;

    /**
     * 从SystemConfig表读取配置值，带默认值
     */
    private String getConfigValue(String key, String defaultValue) {
        String value = systemConfigService.getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String role = request.getRole();
        String username = request.getUsername();
        String password = request.getPassword();

        // 校验角色参数合法性
        if (!"admin".equals(role) && !"librarian".equals(role) && !"reader".equals(role)) {
            throw new BusinessException("无效的角色类型");
        }

        // 验证码校验
        if (request.getCaptcha() != null && request.getCaptchaKey() != null) {
            String redisKey = "captcha:" + request.getCaptchaKey();
            String expectedCode = redisTemplate.opsForValue().get(redisKey);
            if (expectedCode == null) {
                throw new BusinessException("验证码已过期");
            }
            // 验证后删除验证码（一次性）
            redisTemplate.delete(redisKey);
            if (!request.getCaptcha().equalsIgnoreCase(expectedCode)) {
                throw new BusinessException("验证码错误");
            }
        }

        if ("admin".equals(role)) {
            Admin admin = adminMapper.findByUsername(username);
            if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
                String token = jwtUtil.generateToken(username, role, admin.getId());
                asyncSaveLoginLog(username, role, 1, null);
                return new LoginResponse(token, role, username, admin.getRealName(), admin.getId());
            }
        } else if ("librarian".equals(role)) {
            Librarian librarian = librarianMapper.findByUsername(username);
            if (librarian != null && passwordEncoder.matches(password, librarian.getPassword())) {
                String token = jwtUtil.generateToken(username, role, librarian.getId());
                asyncSaveLoginLog(username, role, 1, null);
                return new LoginResponse(token, role, username, librarian.getRealName(), librarian.getId());
            }
        } else if ("reader".equals(role)) {
            Reader reader = readerMapper.findByReaderId(username);
            if (reader != null && passwordEncoder.matches(password, reader.getPassword())) {
                String token = jwtUtil.generateToken(username, role, reader.getId());
                asyncSaveLoginLog(username, role, 1, null);
                return new LoginResponse(token, role, username, reader.getRealName(), reader.getId(), reader.getLanguage());
            }
        }

        // 登录失败，异步写入审计日志
        asyncSaveLoginLog(username, role, 0, "用户名或密码错误");
        throw new BusinessException("用户名或密码错误");
    }

    /**
     * 异步写入登录审计日志
     */
    @Async("logExecutor")
    public void asyncSaveLoginLog(String username, String role, Integer status, String failReason) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(username);
            loginLog.setRole(role);
            loginLog.setLoginTime(java.time.LocalDateTime.now());
            loginLog.setIp(getClientIp());
            loginLog.setUserAgent(getUserAgent());
            loginLog.setStatus(status);
            loginLog.setFailReason(failReason);
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("异步写入登录日志失败: username={}, role={}", username, role, e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取User-Agent
     */
    private String getUserAgent() {
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua;
    }

    @Override
    public Reader register(Reader reader) {
        // 学号格式校验（8位数字）
        if (reader.getReaderId() == null || !reader.getReaderId().matches("\\d{8}")) {
            throw new BusinessException("学号格式无效，需为8位数字");
        }
        // Check if reader already exists
        Reader existingReader = readerMapper.findByReaderId(reader.getReaderId());
        if (existingReader != null) {
            throw new BusinessException("该读者ID已存在");
        }

        // Encode password
        reader.setPassword(passwordEncoder.encode(reader.getPassword()));

        // Set default values from SystemConfig
        int maxBorrowCount = Integer.parseInt(getConfigValue("library.reader.max-borrow-count", "5"));
        reader.setMaxBorrowCount(maxBorrowCount);
        reader.setCurrentBorrowCount(0);
        reader.setFineAmount(new java.math.BigDecimal("0.00"));
        reader.setStatus(1);

        // Insert reader
        readerMapper.insert(reader);

        return reader;
    }

    @Override
    public void changePassword(String userType, Integer userId, String oldPassword, String newPassword) {
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        switch (userType.toUpperCase()) {
            case "ADMIN" -> {
                String currentPassword = adminMapper.findPasswordById(userId);
                if (currentPassword == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!passwordEncoder.matches(oldPassword, currentPassword)) {
                    throw new BusinessException("旧密码错误");
                }
                adminMapper.updatePassword(userId, encodedNewPassword);
                log.info("管理员修改密码成功: userId={}", userId);
            }
            case "LIBRARIAN" -> {
                String currentPassword = librarianMapper.findPasswordById(userId);
                if (currentPassword == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!passwordEncoder.matches(oldPassword, currentPassword)) {
                    throw new BusinessException("旧密码错误");
                }
                librarianMapper.updatePassword(userId, encodedNewPassword);
                log.info("馆员修改密码成功: userId={}", userId);
            }
            case "READER" -> {
                Reader reader = readerMapper.findById(userId);
                if (reader == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!passwordEncoder.matches(oldPassword, reader.getPassword())) {
                    throw new BusinessException("旧密码错误");
                }
                readerMapper.updatePassword(userId, encodedNewPassword);
                log.info("读者修改密码成功: userId={}", userId);
            }
            default -> throw new BusinessException("无效的用户类型");
        }
    }
}
