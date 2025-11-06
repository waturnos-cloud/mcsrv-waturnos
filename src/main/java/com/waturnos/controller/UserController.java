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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.UserDTO;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.mapper.UserMapper;
import com.waturnos.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * The Class UserController.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	
	/** The service. */
	private final UserService service;
	
	private final UserMapper mapper;


	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping("/list/{organizationId}")
	public ResponseEntity<List<UserDTO>> getAll(@PathVariable(required = true) Long organizationId, @RequestParam(required = false) UserRole role) {
		return ResponseEntity.ok(service.findAll(role, organizationId).stream().map(u -> mapper.toDto(u)).toList());
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
	 * Update.
	 *
	 * @param id the id
	 * @param manager the manager
	 * @return the response entity
	 */
	@PostMapping("/managers/{id}")
	public ResponseEntity<ApiResponse<UserDTO>> addManager(@PathVariable Long id,
			@RequestBody UserDTO manager) {
		User managerDB = service.createManager(id, mapper.toEntity(manager));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization add manager", mapper.toDto(managerDB)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param user the user
	 * @return the response entity
	 */
	@PutMapping
	public ResponseEntity<ApiResponse<UserDTO>> update(@RequestBody UserDTO user) {
		User updated = service.update(mapper.toEntity(user));
		return ResponseEntity.ok(new ApiResponse<>(true, "User updated", mapper.toDto(updated)));
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
