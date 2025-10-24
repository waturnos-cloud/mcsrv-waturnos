package com.waturnos.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                tokenProvider.validateToken(token); // throws if invalid

                Long userId = tokenProvider.getUserIdFromToken(token);
                Long tenantId = tokenProvider.getTenantIdFromToken(token);
                String roles = tokenProvider.getRolesFromToken(token); // comma separated

                // set TenantContext for services to use
                TenantContext.setTenantId(tenantId);

                // build Spring Authentication
                List<SimpleGrantedAuthority> authorities = List.of();
                if (roles != null && !roles.isEmpty()) {
                    authorities = Arrays.stream(roles.split(","))
                            .map(String::trim)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException ex) {
            // token inválido: respond 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"JWT inválido\"}");
            return;
        } catch (Exception ex) {
            // limpiar contexto y dejar pasar (o bloquear)
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // limpiar thread local al terminar
            TenantContext.clear();
        }
    }
}