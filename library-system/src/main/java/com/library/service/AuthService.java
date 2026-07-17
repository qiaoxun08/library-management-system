package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.Reader;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Reader register(Reader reader);

    /**
     * 修改密码
     * @param userType 用户类型：ADMIN / LIBRARIAN / READER
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String userType, Integer userId, String oldPassword, String newPassword);
}
