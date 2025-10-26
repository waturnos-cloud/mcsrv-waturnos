package com.waturnos.controller;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.entity.Organization;
import com.waturnos.enums.UserRole;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The Class OrganizationController.
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {
	
	/** The service. */
	private final OrganizationService service;
	
	/** The mapper. */
	private final OrganizationMapper mapper;

	/**
	 * Instantiates a new organization controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public OrganizationController(OrganizationService s, OrganizationMapper m) {
		this.service = s;
		this.mapper = m;
	}

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping
	public ResponseEntity<List<OrganizationDTO>> getAll() {
		return ResponseEntity.ok(service.findAll().stream().map(mapper::toDto).toList());
	}

	/**
	 * Gets the by id.
	 *
	 * @param id the id
	 * @return the by id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id) {
		return service.findById(id).map(mapper::toDto).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	@RequireRole({UserRole.ADMIN})
	public ResponseEntity<ApiResponse<OrganizationDTO>> create(@RequestBody OrganizationDTO dto) {
		Organization created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization created", mapper.toDto(created)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<OrganizationDTO>> update(@PathVariable Long id,
			@RequestBody OrganizationDTO dto) {
		Organization updated = service.update(id, mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", mapper.toDto(updated)));
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
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization deleted", null));
	}
}
