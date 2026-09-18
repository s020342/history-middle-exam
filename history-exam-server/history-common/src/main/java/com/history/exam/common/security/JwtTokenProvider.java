package com.history.exam.common.security;

import com.history.exam.common.context.CurrentUser;
import com.history.exam.common.context.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 * <p>负责生成与解析 token，承载学生端与管理端的统一鉴权凭证。</p>
 */
@Component
public class JwtTokenProvider {

    /** 学生端 token 有效期：30 天（毫秒） */
    private static final long STUDENT_EXPIRE_MILLIS = 30L * 24 * 60 * 60 * 1000;

    /** 管理端 token 有效期：2 小时（毫秒） */
    private static final long ADMIN_EXPIRE_MILLIS = 2L * 60 * 60 * 1000;

    /** JWT 签名密钥（生产环境必须通过环境变量注入，禁止硬编码） */
    @Value("${history.jwt.secret:history-exam-jwt-secret-key-change-me-in-prod}")
    private String secret;

    /** 签名密钥对象 */
    private SecretKey key;

    /**
     * 初始化签名密钥：从配置的字符串密钥派生 HMAC-SHA 密钥
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为学生端生成 token
     *
     * @param studentId 学生 ID
     * @param openid    微信 openid
     * @return JWT token
     */
    public String generateStudentToken(Long studentId, String openid) {
        return generateToken(studentId, openid, UserType.STUDENT, STUDENT_EXPIRE_MILLIS);
    }

    /**
     * 为管理端生成 token
     *
     * @param adminId  管理员 ID
     * @param username 管理员账号
     * @return JWT token
     */
    public String generateAdminToken(Long adminId, String username) {
        return generateToken(adminId, username, UserType.ADMIN, ADMIN_EXPIRE_MILLIS);
    }

    /**
     * 通用 token 生成方法
     *
     * @param userId     用户 ID
     * @param subject    subject（openid 或 username）
     * @param userType   用户类型
     * @param expireMillis 过期时长（毫秒）
     * @return JWT token
     */
    private String generateToken(Long userId, String subject, UserType userType, long expireMillis) {
        Instant now = Instant.now();
        Instant expireAt = now.plusMillis(expireMillis);
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("userId", userId);
        claims.put("userType", userType.name());
        claims.put("expireAt", expireAt.toEpochMilli());
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token 并封装为登录用户上下文
     *
     * @param token JWT token
     * @return 登录用户上下文；解析失败返回 null
     */
    public CurrentUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                return null;
            }
            UserType userType = UserType.valueOf(claims.get("userType", String.class));
            Long expireAt = claims.get("expireAt", Long.class);
            // 学生端附带 openid；管理端附带 username
            CurrentUser.CurrentUserBuilder builder = CurrentUser.builder()
                    .userId(userId)
                    .userType(userType)
                    .expireAt(expireAt);
            if (userType == UserType.STUDENT) {
                builder.openid(claims.getSubject());
            } else {
                builder.username(claims.getSubject());
            }
            return builder.build();
        } catch (JwtException e) {
            // token 非法或过期
            return null;
        }
    }

    /**
     * 校验 token 是否有效
     *
     * @param token JWT token
     * @return true 有效；false 无效或过期
     */
    public boolean isValid(String token) {
        return parse(token) != null;
    }
}
