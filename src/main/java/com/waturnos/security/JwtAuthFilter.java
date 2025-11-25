package com.waturnos.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.waturnos.entity.User;
import com.waturnos.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * The Class JwtAuthFilter.
 */
@Component

/**
 * Instantiates a new jwt auth filter.
 *
 * @param jwtUtil the jwt util
 * @param userRepository the user repository
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends GenericFilter {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7772564977220933356L;
	
	/** The jwt util. */
	private final JwtUtil jwtUtil;
    
    /** The user repository. */
    private final UserRepository userRepository;

    /**
     * Do filter.
     *
     * @param request the request
     * @param response the response
     * @param chain the chain
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authHeader = ((HttpServletRequest) request).getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");

            if (jwtUtil.isTokenValid(token)) {
                String role = jwtUtil.getRoleFromToken(token);
                
                if ("CLIENT".equals(role)) {
                    // Handle client authentication
                    String identifier = jwtUtil.getEmailFromToken(token);
                    Long clientId = jwtUtil.getClientIdFromToken(token);
                    Long organizationId = jwtUtil.getOrganizationIdFromToken(token);
                    
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CLIENT");
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);
                    
                    // Create a custom principal for clients (can be a Map or custom object)
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
            }
        }

        chain.doFilter(request, response);
    }
}
