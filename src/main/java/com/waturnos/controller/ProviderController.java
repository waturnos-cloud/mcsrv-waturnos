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
@RequestMapping("/providers")
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
	 * Creates the provider.
	 *
	 * @param id the id
	 * @param provider the provider
	 * @return the response entity
	 */
	@PostMapping("/{id}")
	public ResponseEntity<ApiResponse<ProviderDTO>> createProvider(@PathVariable Long id,
			@RequestBody ProviderDTO provider) {
		Provider providerDB = service.createProvider(id, mapper.toEntity(provider));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization add provider", mapper.toDto(providerDB)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping
	public ResponseEntity<ApiResponse<ProviderDTO>> update(@RequestBody ProviderDTO dto) {
		Provider updated = service.update(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Provider updated", mapper.toDto(updated)));
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the response entity
	 */
	@DeleteMapping("/{organizationId}/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long organizationId, @PathVariable Long id) {
		service.delete(id, organizationId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Provider deleted", null));
	}
}
