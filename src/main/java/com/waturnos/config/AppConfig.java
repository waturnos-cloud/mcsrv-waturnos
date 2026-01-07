package com.waturnos.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.waturnos.security.CustomUserDetailsService;
import com.waturnos.security.JwtAuthFilter;

@Configuration
public class AppConfig {

	private final JwtAuthFilter jwtFilter;
	
	@Value("${cors.allowed-origins:}")
	private List<String> allowedOrigins;

	public AppConfig(JwtAuthFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		
		// Combinar orígenes permitidos desde application.yml con los hardcodeados
		List<String> allAllowedOrigins = new ArrayList<>();
		
		// Orígenes desde application.yml
		if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
			allAllowedOrigins.addAll(allowedOrigins);
			System.out.println("✅ CORS origins from application.yml: " + allowedOrigins);
		}
		
		// Orígenes hardcodeados (para desarrollo local y dominios legacy)
		allAllowedOrigins.addAll(List.of(
		    "http://localhost:*",
		    "https://localhost:*",
		    "http://127.0.0.1:*",
		    "https://127.0.0.1:*",
		    "http://*.waturnos.com.ar:*",
		    "https://*.waturnos.com.ar:*",
		    "https://*.waturnos.com.ar",
		    "http://waturnos.com.ar",
		    "http://www.waturnos.com.ar",
		    "https://waturnos.com.ar",
		    "https://www.waturnos.com.ar",
		    "https://waturnos-admin.vercel.app",
		    "https://*.onrender.com"
		));
		
		System.out.println("✅ Total CORS allowed origins: " + allAllowedOrigins.size());
		
		config.setAllowedOriginPatterns(allAllowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setMaxAge(3600L); // Cache preflight response for 1 hour

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> {
		}) // usa el filtro CORS global
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**", "/api/auth/**", "/swagger-ui.html", "/swagger-ui/**",
								"/api-docs/**", "/v3/api-docs/**", "/public/**", "/images/**")
						.permitAll().anyRequest().authenticated())
				// Manejar errores de autenticación con 401 en lugar de 403
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(401);
							response.setContentType("application/json");
							response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
						}))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder,
			CustomUserDetailsService userDetailsService) throws Exception {

		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		return authenticationManagerBuilder.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}