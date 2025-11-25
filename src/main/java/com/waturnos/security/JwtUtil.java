package com.waturnos.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * The Class JwtUtil.
 */
@Component
public class JwtUtil {
	
	/** The secret. */
	@Value("${jwt.secret}")
	private String secret;
	
	/** The expiration ms. */
	@Value("${jwt.expiration-ms}")
	private long expirationMs;
	
	/** The key. */
	private Key key;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		key = Keys.hmacShaKeyFor(secret.getBytes());
	}

    /**
     * Generate token.
     *
     * @param email the email
     * @param role the role
     * @return the string
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }
    
    /**
     * Generate token for client with additional claims.
     *
     * @param identifier the identifier (email or phone)
     * @param clientId the client id
     * @param organizationId the organization id
     * @return the string
     */
    public String generateClientToken(String identifier, Long clientId, Long organizationId) {
        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", "CLIENT")
                .claim("clientId", clientId)
                .claim("organizationId", organizationId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Gets the email from token.
     *
     * @param token the token
     * @return the email from token
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Gets the role from token.
     *
     * @param token the token
     * @return the role from token
     */
    public String getRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }
    
    /**
     * Gets the client id from token.
     *
     * @param token the token
     * @return the client id from token
     */
    public Long getClientIdFromToken(String token) {
        Object clientId = parseClaims(token).get("clientId");
        return clientId != null ? ((Number) clientId).longValue() : null;
    }
    
    /**
     * Gets the organization id from token.
     *
     * @param token the token
     * @return the organization id from token
     */
    public Long getOrganizationIdFromToken(String token) {
        Object orgId = parseClaims(token).get("organizationId");
        return orgId != null ? ((Number) orgId).longValue() : null;
    }

    /**
     * Checks if is token valid.
     *
     * @param token the token
     * @return true, if is token valid
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Parses the claims.
     *
     * @param token the token
     * @return the claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
