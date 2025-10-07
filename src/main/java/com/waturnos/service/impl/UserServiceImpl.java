package com.waturnos.service.impl;
import com.waturnos.entity.User;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.UserService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List; import java.util.Optional;
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public UserServiceImpl(UserRepository userRepository){this.userRepository=userRepository;}
    @Override public List<User> findAll(){return userRepository.findAll();}
    @Override public Optional<User> findByEmail(String email){return userRepository.findByEmail(email);}
    @Override public User create(User user){
        if(user.getPasswordHash()!=null) user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
    @Override public User update(Long id, User user){
        User existing = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setId(existing.getId());
        if(user.getPasswordHash()!=null && !user.getPasswordHash().isBlank()) user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        else user.setPasswordHash(existing.getPasswordHash());
        return userRepository.save(user);
    }
    @Override public void delete(Long id){
        if(!userRepository.existsById(id)) throw new EntityNotFoundException("User not found");
        userRepository.deleteById(id);
    }
}
