package com.waturnos.controller;

import com.waturnos.dto.beans.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.mapper.ClientMapper;
import com.waturnos.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The Class ClientController.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {
	
	/** The service. */
	private final ClientService service;
	
	/** The mapper. */
	private final ClientMapper mapper;

	/**
	 * Instantiates a new client controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public ClientController(ClientService s, ClientMapper m) {
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
	public ResponseEntity<List<ClientDTO>> getByOrganization(@PathVariable Long orgId) {
		return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ClientDTO>> create(@RequestBody ClientDTO dto) {
		Client created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Client created", mapper.toDto(created)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDTO>> update(@PathVariable Long id, @RequestBody ClientDTO dto) {
		Client updated = service.update(id, mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Client updated", mapper.toDto(updated)));
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
		return ResponseEntity.ok(new ApiResponse<>(true, "Client deleted", null));
	}
}
