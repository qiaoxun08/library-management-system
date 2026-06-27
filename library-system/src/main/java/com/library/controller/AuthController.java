package com.library.controller;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

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
}
