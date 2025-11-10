package com.waturnos.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.waturnos.controller.exceptions.ErrorResponse;
import com.waturnos.dto.request.LoginRequest;
import com.waturnos.dto.response.LoginResponse;
import com.waturnos.entity.User;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.JwtUtil;
import com.waturnos.service.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final UserRepository userRepository; // 👈 necesario para buscar el user completo

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, WebRequest webRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            // Buscar el usuario real para obtener IDs
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("USER");

            String token = jwtUtil.generateToken(email, role);

            // Determinar organizationId y providerId según el rol
            Long organizationId = null;
            Long providerId = null;

            switch (role) {
                case "ADMIN" -> {
                    organizationId = null; // elige luego en el dashboard
                }
                case "MANAGER" -> {
                    organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
                }
                case "PROVIDER" -> {
                    organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
                    providerId = user.getId();
                }
            }

            // ✅ Respuesta extendida
            LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                role,
                organizationId,
                providerId
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            ErrorResponse errorDetails = new ErrorResponse(
                ErrorCode.INVALID_USER_OR_PASSWORD.getCode(),
                messageSource.getMessage(ErrorCode.INVALID_USER_OR_PASSWORD.getMessageKey(), null, LocaleContextHolder.getLocale()),
                webRequest.getDescription(false)
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
        }
    }
}