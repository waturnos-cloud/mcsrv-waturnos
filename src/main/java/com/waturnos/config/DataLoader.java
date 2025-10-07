package com.waturnos.config;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
  @Bean CommandLineRunner seed(UserService userService){
    return args -> {
      userService.findByEmail("admin@demo.com").orElseGet(() -> {
        User u = User.builder().fullName("Admin").email("admin@demo.com").passwordHash("admin").role(UserRole.ADMIN).active(true).build();
        return userService.create(u);
      });
    };
  }
}
