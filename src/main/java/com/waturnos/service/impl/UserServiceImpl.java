package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.waturnos.entity.User;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.UserService;
import com.waturnos.service.exceptions.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	@Override
	public User create(User user) {
		
		
		
		if (user.getPassword() != null)
			user.setPassword(encoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	@Override
	public User update(Long id, User user) {
		User existing = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
		user.setId(existing.getId());
		if (user.getPassword() != null && !user.getPassword().isBlank())
			user.setPassword(encoder.encode(user.getPassword()));
		else
			user.setPassword(existing.getPassword());
		return userRepository.save(user);
	}

	@Override
	public void delete(Long id) {
		if (!userRepository.existsById(id))
			throw new EntityNotFoundException("User not found");
		userRepository.deleteById(id);
	}
}
