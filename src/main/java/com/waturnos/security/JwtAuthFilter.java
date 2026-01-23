package com.waturnos.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.waturnos.entity.User;
import com.waturnos.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class JwtAuthFilter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
	
	/** The jwt util. */
	private final JwtUtil jwtUtil;
    
    /** The user repository. */
    private final UserRepository userRepository;

    /**
     * Should not filter.
     * Excluye endpoints públicos del filtro JWT.
     *
     * @param request the request
     * @return true si el filtro NO debe ejecutarse
     * @throws ServletException the servlet exception
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Endpoints públicos que no requieren JWT
        return path.startsWith("/msvc-waturnos/v1.0/auth/") ||
               path.startsWith("/msvc-waturnos/v1.0/api/auth/") ||
               path.startsWith("/msvc-waturnos/v1.0/webhooks/") ||
               path.startsWith("/msvc-waturnos/v1.0/public/") ||
               path.startsWith("/msvc-waturnos/v1.0/images/") ||
               path.startsWith("/msvc-waturnos/v1.0/swagger-ui") ||
               path.startsWith("/msvc-waturnos/v1.0/api-docs") ||
               path.startsWith("/msvc-waturnos/v1.0/v3/api-docs");
    }

    /**
     * Do filter internal.
     *
     * @param request the request
     * @param response the response
     * @param chain the chain
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");

            try {
                if (jwtUtil.isTokenValid(token)) {
                    String role = jwtUtil.getRoleFromToken(token);
                    
                    if ("CLIENT".equals(role)) {
                        // Handle client authentication
                        String identifier = jwtUtil.getEmailFromToken(token);
                        Long clientId = jwtUtil.getClientIdFromToken(token);
                        Long organizationId = jwtUtil.getOrganizationIdFromToken(token);
                        
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CLIENT");
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);
                        
                        ClientPrincipal clientPrincipal = new ClientPrincipal(clientId, identifier, organizationId);
                        
                        var auth = new UsernamePasswordAuthenticationToken(clientPrincipal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        // Handle user authentication (ADMIN, MANAGER, PROVIDER)
                        String email = jwtUtil.getEmailFromToken(token);
                        User user = userRepository.findByEmail(email).orElse(null);
                        if (user != null) {
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                            List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);
                            user.setIdOrganization(user.getOrganization() != null ? user.getOrganization().getId() : null);
                            var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } else {
                    // Token inválido o expirado: retornar 401
                    log.warn("Token inválido o expirado para la request: {}", request.getRequestURI());
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Token expired or invalid\",\"message\":\"Please login again\"}");
                    return;
                }
            } catch (Exception e) {
                // Error al procesar el token: retornar 401
                log.error("Error procesando token JWT", e);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication failed\",\"message\":\"Invalid token\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
