package com.waturnos.controller;
import com.waturnos.entity.User;
import com.waturnos.security.JwtUtil;
import com.waturnos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final UserService userService; private final JwtUtil jwtUtil; private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  public AuthController(UserService userService, JwtUtil jwtUtil){ this.userService=userService; this.jwtUtil=jwtUtil; }
  @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body){
    String email = body.getOrDefault("email",""); String password = body.getOrDefault("password","");
    return userService.findByEmail(email)
      .filter(u -> encoder.matches(password, u.getPasswordHash()))
      .map(u -> ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(email))))
      .orElse(ResponseEntity.status(401).body(Map.of("error","Invalid credentials")));
  }
}
