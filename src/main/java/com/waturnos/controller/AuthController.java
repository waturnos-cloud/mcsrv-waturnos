package com.waturnos.controller;

import com.waturnos.entity.User; // tu entidad users
import com.waturnos.repository.UserRepository;
import com.waturnos.security.JwtTokenProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        Optional<User> uOpt = userRepository.findByEmail(req.getEmail());
        if (uOpt.isEmpty()) {
            throw new RuntimeException("Usuario/Password inválidos");
        }
        User user = uOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Usuario/Password inválidos");
        }
        Long tenantId = user.getTenant().getTenantId();
        String roles = user.getRole(); // ej: "OWNER" o "STAFF"
        String token = tokenProvider.generateToken(user.getUserId(), tenantId, roles);
        return new LoginResponse(token, tokenProvider.getUserIdFromToken(token), tenantId, roles);
    }

    // DTOs
    public static class LoginRequest {
        private String email;
        private String password;
        // getters/setters
        public String getEmail(){return email;} public void setEmail(String e){email=e;}
        public String getPassword(){return password;} public void setPassword(String p){password=p;}
    }

    public static class LoginResponse {
        private String token;
        private Long userId;
        private Long tenantId;
        private String roles;
        public LoginResponse(String token, Long userId, Long tenantId, String roles) {
            this.token = token; this.userId = userId; this.tenantId = tenantId; this.roles = roles;
        }
        // getters
        public String getToken(){return token;}
        public Long getUserId(){return userId;}
        public Long getTenantId(){return tenantId;}
        public String getRoles(){return roles;}
    }
}