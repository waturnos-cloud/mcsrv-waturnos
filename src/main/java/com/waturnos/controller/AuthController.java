package com.waturnos.controller;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.security.JwtUtil;
import com.waturnos.service.UserService;

/**
 * The Class AuthController.
 */
@RestController @RequestMapping("/api/auth")
public class AuthController {
  
  /** The user service. */
  private final UserService userService; 
 /** The jwt util. */
 private final JwtUtil jwtUtil; 
 /** The encoder. */
 private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  
  /**
   * Instantiates a new auth controller.
   *
   * @param userService the user service
   * @param jwtUtil the jwt util
   */
  public AuthController(UserService userService, JwtUtil jwtUtil){ this.userService=userService; this.jwtUtil=jwtUtil; }
  
  /**
   * Login.
   *
   * @param body the body
   * @return the response entity
   */
  @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body){
    String email = body.getOrDefault("email",""); String password = body.getOrDefault("password","");
    return userService.findByEmail(email)
      .filter(u -> encoder.matches(password, u.getPasswordHash()))
      .map(u -> ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(email))))
      .orElse(ResponseEntity.status(401).body(Map.of("error","Invalid credentials")));
  }
}
