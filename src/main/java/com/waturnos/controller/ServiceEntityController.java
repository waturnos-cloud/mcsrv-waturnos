package com.waturnos.controller;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.mapper.ServiceMapper;
import com.waturnos.service.ServiceEntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The Class ServiceEntityController.
 */
@RestController
@RequestMapping("/services")
public class ServiceEntityController {
	
	/** The service. */
	private final ServiceEntityService service;
	
	/** The mapper. */
	private final ServiceMapper mapper;

	/**
	 * Instantiates a new service entity controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public ServiceEntityController(ServiceEntityService s, ServiceMapper m) {
		this.service = s;
		this.mapper = m;
	}

	/**
	 * Gets the by provider.
	 *
	 * @param providerId the provider id
	 * @return the by provider
	 */
	@GetMapping("/provider/{providerId}")
	public ResponseEntity<List<ServiceDTO>> getByProvider(@PathVariable Long providerId) {
		return ResponseEntity.ok(service.findByProvider(providerId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Gets the by location.
	 *
	 * @param locationId the location id
	 * @return the by location
	 */
	@GetMapping("/location/{locationId}")
	public ResponseEntity<List<ServiceDTO>> getByLocation(@PathVariable Long locationId) {
		return ResponseEntity.ok(service.findByLocation(locationId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ServiceDTO>> create(@RequestBody ServiceDTO dto) {
		ServiceEntity created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Service created", mapper.toDto(created)));
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ServiceDTO>> update(@PathVariable Long id, @RequestBody ServiceDTO dto) {
		ServiceEntity updated = service.update(id, mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Service updated", mapper.toDto(updated)));
	}
}
