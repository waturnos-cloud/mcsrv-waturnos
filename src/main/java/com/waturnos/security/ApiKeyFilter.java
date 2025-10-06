package com.waturnos.security;

import com.waturnos.service.TenantService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {

    private static final String HEADER = "X-API-KEY";
    private final TenantService tenantService;

    public ApiKeyFilter(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String key = req.getHeader(HEADER);

        if (key == null || tenantService.findByApiKey(key).isEmpty()) {
            throw new ServletException("API Key inválida o ausente");
        }

        chain.doFilter(request, response);
    }
}