package com.waturnos.controller;
import com.waturnos.entity.User;
import com.waturnos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/users")
public class UserController {
  private final UserService service;
  public UserController(UserService s){this.service=s;}
  @GetMapping public ResponseEntity<List<User>> getAll(){ return ResponseEntity.ok(service.findAll()); }
  @PostMapping public ResponseEntity<ApiResponse<User>> create(@RequestBody User user){
    User created = service.create(user); return ResponseEntity.ok(new ApiResponse<>(true,"User created", created));
  }
  @PutMapping("/{id}") public ResponseEntity<ApiResponse<User>> update(@PathVariable Long id, @RequestBody User user){
    User updated = service.update(id, user); return ResponseEntity.ok(new ApiResponse<>(true,"User updated", updated));
  }
  @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
    service.delete(id); return ResponseEntity.ok(new ApiResponse<>(true,"User deleted", null));
  }
}
