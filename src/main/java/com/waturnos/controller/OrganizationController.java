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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.beans.ProviderDTO;
import com.waturnos.dto.beans.UserDTO;
import com.waturnos.dto.request.CreateOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.mapper.ProviderMapper;
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
	
	/** The provider mapper. */
	private final ProviderMapper providerMapper;

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
				userMapper.toEntity(createOrganization.getManager()),createOrganization.isSimpleOrganization());
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization created", organizationMapper.toDto(created,true)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/update/{id}")
	public ResponseEntity<ApiResponse<OrganizationDTO>> updateBasicInfo(@PathVariable Long id,
			@RequestBody OrganizationDTO dto) {
		Organization updated = service.updateBasicInfo(id, organizationMapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", organizationMapper.toDto(updated,false)));
	}
	
	/**
	 * Update locations.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/updatelocations/{id}")
	public ResponseEntity<ApiResponse<OrganizationDTO>> updateLocations(@PathVariable Long id,
			@RequestBody OrganizationDTO dto) {
		Organization updated = service.updateLocations(id, organizationMapper.mapLocationsToEntity(dto.getLocations()));
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
	 * Update.
	 *
	 * @param id the id
	 * @param manager the manager
	 * @return the response entity
	 */
	@PostMapping("/managers/{id}")
	public ResponseEntity<ApiResponse<UserDTO>> addManager(@PathVariable Long id,
			@RequestBody UserDTO manager) {
		User managerDB = service.addManager(id, userMapper.toEntity(manager));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization add manager", userMapper.toDto(managerDB)));
	}
	
	/**
	 * Update.
	 *
	 * @param id the id
	 * @param provider the provider
	 * @return the response entity
	 */
	@PostMapping("/providers/{id}")
	public ResponseEntity<ApiResponse<ProviderDTO>> addProvider(@PathVariable Long id,
			@RequestBody ProviderDTO provider) {
		Provider providerDB = service.addProvider(id, providerMapper.toEntity(provider));
		return ResponseEntity.ok(new ApiResponse<>(true, "Organization add provider", providerMapper.toDto(providerDB)));
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
