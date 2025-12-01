package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.request.CreateOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.mapper.UserMapper;
import com.waturnos.service.OrganizationService;

import lombok.RequiredArgsConstructor;

/**
 * The Class OrganizationController.
 */
@RestController
@RequestMapping("/organizations")

/**
 * Instantiates a new organization controller.
 *
 * @param service the service
 * @param organizationMapper the organization mapper
 * @param userMapper the user mapper
 */
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
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		return ResponseEntity.ok(service.findAll(authentication).stream().map(o -> organizationMapper.toDto(o,false)).toList());
	}

	/**
	 * Gets the by id.
	 *
	 * @param id the id
	 * @return the by id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id) {
		return service.findById(id).map(o -> organizationMapper.toDto(o, true)).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Creates the.
	 *
	 * @param createOrganization the create organization
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<OrganizationDTO>> create(@RequestBody CreateOrganization createOrganization) {
		Organization created = service.create(organizationMapper.toEntity(createOrganization.getOrganization()),
				userMapper.toEntity(createOrganization.getManager()));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization created", organizationMapper.toDto(created,true)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/update")
	public ResponseEntity<ApiResponse<OrganizationDTO>> updateBasicInfo(
			@RequestBody OrganizationDTO dto) {
		Organization updated = service.updateBasicInfo(organizationMapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", organizationMapper.toDto(updated,false)));
	}
	
	/**
	 * Update locations.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/updatelocations")
	public ResponseEntity<ApiResponse<OrganizationDTO>> updateLocations(
			@RequestBody OrganizationDTO dto) {
		Organization updated = service.updateLocations(dto.getId(), organizationMapper.mapLocationsToEntity(dto.getLocations()));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", organizationMapper.toDto(updated, true)));
	}
	
	/**
	 * Update.
	 *
	 * @param id the id
	 * @param organizationStatus the organization status
	 * @return the response entity
	 */
	@PutMapping("/activate/{id}/{organizationStatus}")
	public ResponseEntity<ApiResponse<OrganizationDTO>> updateBasicInfo(@PathVariable Long id,
			@PathVariable OrganizationStatus organizationStatus) {
		Organization updated = service.activateOrDeactivate(id, organizationStatus);
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", organizationMapper.toDto(updated,false)));
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

	/**
	 * Checks if subdomain exists. Optionally excludes an organization id.
	 *
	 * @param subdomain the subdomain to check
	 * @param organizationId optional org id to exclude
	 * @return true if exists in other orgs; false otherwise
	 */
	@GetMapping("/subdomain/check")
	public ResponseEntity<ApiResponse<Boolean>> checkSubdomain(
			@RequestParam("subdomain") String subdomain,
			@RequestParam(value = "organizationId", required = false) Long organizationId) {
		boolean exists = service.subdomainExists(subdomain, organizationId);
		boolean available = !exists;
		return ResponseEntity.ok(new ApiResponse<>(true, "Subdomain availability", available));
	}
}
