package com.example.skhubox.security.jwt;

import com.example.skhubox.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

// HMAC-SHA256 requires a key of at least 32 bytes

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HMAC-SHA256");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getUser().getRole().name())
                .claim("authorities", List.of("ROLE_" + userDetails.getUser().getRole().name()))
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public String getStudentNumber(String token) {
        return getClaims(token).getSubject();
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
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

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}
