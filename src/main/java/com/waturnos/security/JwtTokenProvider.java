package com.waturnos.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long expirationMs) {
        // support plain base64 or plain secret
        byte[] keyBytes = secret.length() > 32 ? Decoders.BASE64.decode(secret) : secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, Long tenantId, String roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(Map.of(
                    "tenantId", tenantId,
                    "roles", roles
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token).getBody();
        return Long.valueOf(claims.getSubject());
    }

    public Long getTenantIdFromToken(String token) {
        Claims claims = validateToken(token).getBody();
        Object t = claims.get("tenantId");
        if (t == null) return null;
        return ((Number) t).longValue();
    }

    public String getRolesFromToken(String token) {
        Claims claims = validateToken(token).getBody();
        return (String) claims.get("roles");
    }
}