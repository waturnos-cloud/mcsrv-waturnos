package com.waturnos.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.waturnos.controller.exceptions.ErrorResponse;
import com.waturnos.dto.request.LoginRequest;
import com.waturnos.dto.response.LoginResponse;
import com.waturnos.security.CustomUserDetails;
import com.waturnos.security.JwtUtil;
import com.waturnos.service.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * The Class AuthController.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	/** The jwt util. */
	private final JwtUtil jwtUtil;

	private final AuthenticationManager authenticationManager;

	private final MessageSource messageSource;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, WebRequest webRequest) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String email = userDetails.getUsername();
			String role = userDetails.getAuthorities().stream().findFirst()
					.map(auth -> auth.getAuthority().replace("ROLE_", "")).orElse("USER");

			String token = jwtUtil.generateToken(email, role);

			return ResponseEntity.ok(new LoginResponse(token, userDetails.getId(), userDetails.getOrganizationId(),
					userDetails.getOrganizationName(), role, userDetails.getSimpleOrganization()));

		} catch (AuthenticationException e) {
			ErrorResponse errorDetails = new ErrorResponse(ErrorCode.INVALID_USER_OR_PASSWORD.getCode(),
					messageSource.getMessage(ErrorCode.INVALID_USER_OR_PASSWORD.getMessageKey(), null,
							LocaleContextHolder.getLocale()),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
		}
	}
}
