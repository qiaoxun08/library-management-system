package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.Reader;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Reader register(Reader reader);
}
