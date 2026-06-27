package com.library.controller;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.AuthService;
import com.library.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "登录、注册相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    @Operation(summary = "读者注册", description = "新读者注册账号")
    public Result<Reader> register(@Valid @RequestBody Reader reader) {
        Reader newReader = authService.register(reader);
        return Result.success(newReader);
    }

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 刷新 Token：传入旧 Token，返回新 Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "传入旧Token获取新Token，用于无感续期")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> body) {
        String oldToken = body.get("token");
        if (oldToken == null || oldToken.isEmpty()) {
            return Result.error(400, "Token不能为空");
        }
        String newToken = jwtUtil.refreshToken(oldToken);
        if (newToken == null) {
            return Result.error(401, "Token无效或已过期");
        }
        return Result.success(Map.of("token", newToken));
    }
}
