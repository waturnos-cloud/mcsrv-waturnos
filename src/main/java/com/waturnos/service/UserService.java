package com.waturnos.service;
import com.waturnos.entity.User;
import java.util.List; import java.util.Optional;
public interface UserService {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
}
