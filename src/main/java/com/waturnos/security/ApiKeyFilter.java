package com.waturnos.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.waturnos.entity.Tenant;
import com.waturnos.repository.TenantRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends OncePerRequestFilter {

	private final TenantRepository tenantRepository;

	public ApiKeyFilter(TenantRepository tenantRepository) {
		this.tenantRepository = tenantRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		// 🔹 ignorar endpoints públicos
		if (path.startsWith("/auth") || path.startsWith("/actuator")) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = request.getHeader("X-API-KEY");

		// si no hay apiKey, dejamos seguir (JWT puede manejarlo)
		if (apiKey == null || apiKey.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		Tenant tenant = tenantRepository.findByApiKey(apiKey);
		if (tenant == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\":\"API Key inválida\"}");
			return;
		}
		TenantContext.setTenantId(tenant.getTenantId());

		try {
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}
}