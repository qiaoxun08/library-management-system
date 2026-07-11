package com.library.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * 生成 JWT Token（RBAC 版本）
     * @param username 用户名
     * @param userType 用户类型：READER/ADMIN/LIBRARIAN
     * @param userId 用户ID
     */
    public String generateToken(String username, String userType, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userType);
        claims.put("userType", userType);
        claims.put("userId", userId);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("role");
    }

    public Boolean isTokenExpired(String token) {
        try {
            return getAllClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新 Token：验证旧 Token 有效后签发新 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String username = claims.getSubject();
            String userType = claims.get("userType", String.class);
            Integer userId = claims.get("userId", Integer.class);
            // 优先使用三参数版本（包含 userId 和 userType），确保 RBAC 过滤器正常工作
            if (userType != null && userId != null) {
                return generateToken(username, userType, userId);
            }
            // 兼容旧版 Token（只有 role）
            String role = (String) claims.get("role");
            return generateToken(username, role);
        } catch (Exception e) {
            return null;
        }
    }
}
