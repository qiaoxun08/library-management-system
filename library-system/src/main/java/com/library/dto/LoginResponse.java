package com.library.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private String realName;
    private Integer id;
    private String language;

    public LoginResponse(String token, String role, String username, String realName) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.realName = realName;
    }

    public LoginResponse(String token, String role, String username, String realName, Integer id) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.realName = realName;
        this.id = id;
    }

    public LoginResponse(String token, String role, String username, String realName, Integer id, String language) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.realName = realName;
        this.id = id;
        this.language = language;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
