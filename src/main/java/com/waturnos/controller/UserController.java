package com.waturnos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.entity.User;
import com.waturnos.service.UserService;

/**
 * The Class UserController.
 */
@RestController
@RequestMapping("/users")
public class UserController {
	
	/** The service. */
	private final UserService service;

	/**
	 * Instantiates a new user controller.
	 *
	 * @param s the s
	 */
	public UserController(UserService s) {
		this.service = s;
	}

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping
	public ResponseEntity<List<User>> getAll() {
		return ResponseEntity.ok(service.findAll());
	}

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Optional<User>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	/**
	 * Creates the.
	 *
	 * @param user the user
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<User>> create(@RequestBody User user) {
		User created = service.create(user);
		return ResponseEntity.ok(new ApiResponse<>(true, "User created", created));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param user the user
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<User>> update(@PathVariable Long id, @RequestBody User user) {
		User updated = service.update(id, user);
		return ResponseEntity.ok(new ApiResponse<>(true, "User updated", updated));
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the response entity
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "User deleted", null));
	}
}
