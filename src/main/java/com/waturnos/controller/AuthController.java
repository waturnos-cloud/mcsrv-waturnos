package com.waturnos.controller;
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

import com.waturnos.dto.request.LoginRequest;
import com.waturnos.dto.response.LoginResponse;
import com.waturnos.security.JwtUtil;
import com.waturnos.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * The Class AuthController.
 */
@RestController @RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  
  /** The user service. */
  private final UserService userService; 
 /** The jwt util. */
 private final JwtUtil jwtUtil; 
 
 private final AuthenticationManager authenticationManager;
  
  
  
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
  	try {
          Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
          );
          UserDetails userDetails = (UserDetails) authentication.getPrincipal();
          String email = userDetails.getUsername(); // username = email
          String role = userDetails.getAuthorities().stream()
                          .findFirst()
                          .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                          .orElse("USER");

          String token = jwtUtil.generateToken(email, role); // Usar tu firma real

          return ResponseEntity.ok(new LoginResponse(token));
      } catch (AuthenticationException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
      }
  }
}
