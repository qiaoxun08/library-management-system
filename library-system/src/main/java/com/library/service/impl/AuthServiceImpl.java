package com.library.service.impl;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.Admin;
import com.library.entity.Librarian;
import com.library.entity.Reader;
import com.library.mapper.AdminMapper;
import com.library.mapper.LibrarianMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.AuthService;
import com.library.service.SystemConfigService;
import com.library.exception.BusinessException;
import com.library.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

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
                String token = jwtUtil.generateToken(username, role);
                return new LoginResponse(token, role, username, admin.getRealName(), admin.getId());
            }
        } else if ("librarian".equals(role)) {
            Librarian librarian = librarianMapper.findByUsername(username);
            if (librarian != null && passwordEncoder.matches(password, librarian.getPassword())) {
                String token = jwtUtil.generateToken(username, role);
                return new LoginResponse(token, role, username, librarian.getRealName(), librarian.getId());
            }
        } else if ("reader".equals(role)) {
            Reader reader = readerMapper.findByReaderId(username);
            if (reader != null && passwordEncoder.matches(password, reader.getPassword())) {
                String token = jwtUtil.generateToken(username, role);
                return new LoginResponse(token, role, username, reader.getRealName(), reader.getId(), reader.getLanguage());
            }
        }

        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public Reader register(Reader reader) {
        // Check if reader already exists
        Reader existingReader = readerMapper.findByReaderId(reader.getReaderId());
        if (existingReader != null) {
            throw new RuntimeException("该读者ID已存在");
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
}
