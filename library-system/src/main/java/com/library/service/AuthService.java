package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.Reader;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Reader register(Reader reader);

    /**
     * 刷新 Token：校验旧 Token 有效且账户未被禁用后签发新 Token
     * @param oldToken 旧 Token
     * @return 新 Token，无效/过期/账户已禁用时返回 null
     */
    String refreshUserToken(String oldToken);

    /**
     * 修改密码
     * @param userType 用户类型：ADMIN / LIBRARIAN / READER
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String userType, Integer userId, String oldPassword, String newPassword);
}
