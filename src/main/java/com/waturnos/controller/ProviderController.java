package com.waturnos.controller;

import com.waturnos.dto.beans.ProviderDTO;
import com.waturnos.entity.Provider;
import com.waturnos.mapper.ProviderMapper;
import com.waturnos.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The Class ProviderController.
 */
@RestController
@RequestMapping("/api/providers")
public class ProviderController {
	
	/** The service. */
	private final ProviderService service;
	
	/** The mapper. */
	private final ProviderMapper mapper;

	/**
	 * Instantiates a new provider controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public ProviderController(ProviderService s, ProviderMapper m) {
		this.service = s;
		this.mapper = m;
	}

	/**
	 * Gets the by organization.
	 *
	 * @param orgId the org id
	 * @return the by organization
	 */
	@GetMapping("/organization/{orgId}")
	public ResponseEntity<List<ProviderDTO>> getByOrganization(@PathVariable Long orgId) {
		return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ProviderDTO>> create(@RequestBody ProviderDTO dto) {
		Provider created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Provider created", mapper.toDto(created)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProviderDTO>> update(@PathVariable Long id, @RequestBody ProviderDTO dto) {
		Provider updated = service.update(id, mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Provider updated", mapper.toDto(updated)));
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
		return ResponseEntity.ok(new ApiResponse<>(true, "Provider deleted", null));
	}
}
