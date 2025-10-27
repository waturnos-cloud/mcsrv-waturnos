package com.waturnos.config;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;

import com.waturnos.security.CustomUserDetailsService;
import com.waturnos.security.JwtAuthFilter;

@Configuration
public class AppConfig {

	private final JwtAuthFilter jwtFilter;

	public AppConfig(JwtAuthFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(cors -> cors.configurationSource(request -> {
					CorsConfiguration config = new CorsConfiguration();
					config.setAllowedOrigins(List.of("http://localhost:5173", // Front local
							"http://localhost:5174", // Alternativo (si usás otro puerto)
							"https://waturnos-admin.vercel.app" // Front desplegado
					));
					config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
					config.setAllowedHeaders(
							List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
					config.setExposedHeaders(List.of("Authorization"));
					config.setAllowCredentials(true);
					return config;
				})).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**", "/swagger-ui.html",
						"/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll().anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
				
				
		return http.build();
	}
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       PasswordEncoder passwordEncoder,
                                                       CustomUserDetailsService userDetailsService) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Crea un DelegatingPasswordEncoder con los mapeos predeterminados de Spring Security.
        // Este reconocerá el prefijo {bcrypt} automáticamente.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
