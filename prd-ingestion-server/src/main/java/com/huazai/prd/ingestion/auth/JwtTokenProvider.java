package com.huazai.prd.ingestion.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT Token 生成与验证工具。
 */
@Component
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long tokenValidityMs;

    public JwtTokenProvider(
            @Value("${auth.jwt.secret:HuazaiPrdIngestionJwtSecretKey2026!@#$%^&*()VeryLong}") String secret,
            @Value("${auth.jwt.token-validity-ms:86400000}") long tokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityMs = tokenValidityMs;
    }

    /** 生成 JWT token。 */
    public String createToken(String userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityMs);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /** 从 token 中提取用户 ID。 */
    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /** 从 token 中提取用户名。 */
    public String getUsernameFromToken(String token) {
        return getClaims(token).get("username", String.class);
    }

    /** 从 token 中提取角色列表。 */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaims(token).get("roles", List.class);
    }

    /** 验证 token 是否有效。 */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            LOG.warn("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}