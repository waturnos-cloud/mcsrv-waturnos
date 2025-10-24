package com.waturnos.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.expiration-ms}")
	private long expirationMs;
	private Key key;

	@PostConstruct
	public void init() {
		key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String subject) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(exp)
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String getSubject(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}
}
