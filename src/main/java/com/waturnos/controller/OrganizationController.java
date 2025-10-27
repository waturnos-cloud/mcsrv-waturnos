package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.request.CreateOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.mapper.UserMapper;
import com.waturnos.service.OrganizationService;

import lombok.RequiredArgsConstructor;

/**
 * The Class OrganizationController.
 */
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {
	
	/** The service. */
	private final OrganizationService service;
	
	/** The mapper. */
	private final OrganizationMapper organizationMapper;
	
	/** The user mapper. */
	private final UserMapper userMapper;

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping
	public ResponseEntity<List<OrganizationDTO>> getAll() {
		return ResponseEntity.ok(service.findAll().stream().map(organizationMapper::toDto).toList());
	}

	/**
	 * Gets the by id.
	 *
	 * @param id the id
	 * @return the by id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id) {
		return service.findById(id).map(organizationMapper::toDto).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Creates the.
	 *
	 * @param createOrganization.getOrganization() the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<OrganizationDTO>> create(@RequestBody CreateOrganization createOrganization) {
		Organization created = service.create(organizationMapper.toEntity(createOrganization.getOrganization()),
				userMapper.toEntity(createOrganization.getManager()),createOrganization.isSimpleOrganization());
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization created", organizationMapper.toDto(created)));
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
		Organization updated = service.update(id, organizationMapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", organizationMapper.toDto(updated)));
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
