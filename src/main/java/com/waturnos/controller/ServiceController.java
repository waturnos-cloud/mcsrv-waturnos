package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.dto.request.CreateService;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.mapper.AvailabilityMapper;
import com.waturnos.mapper.ServiceMapper;
import com.waturnos.service.ServiceEntityService;

/**
 * The Class ServiceEntityController.
 */
@RestController
@RequestMapping("/services")
public class ServiceController {

	/** The service. */
	private final ServiceEntityService service;

	/** The mapper. */
	private final ServiceMapper serviceMapper;
	private final AvailabilityMapper availabilityMapper;

	/**
	 * Instantiates a new service entity controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public ServiceController(ServiceEntityService s, ServiceMapper m, AvailabilityMapper am) {
		this.service = s;
		this.serviceMapper = m;
		this.availabilityMapper = am;
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ServiceDTO>> create(@RequestBody CreateService createService) {
		ServiceEntity created = service.create(serviceMapper.toEntity(createService.getServiceDto()),
				availabilityMapper.toEntityList(createService.getListAvailability()),
				createService.getServiceDto().getUser().getId(), createService.getServiceDto().getLocation().getId(),
				createService.isWorkInHollidays());
		return ResponseEntity.ok(new ApiResponse<>(true, "Service created", serviceMapper.toDTO(created)));
	}

	/**
	 * Gets the by user id.
	 *
	 * @param userId the user id
	 * @return the by user id
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ServiceDTO>> getByUserId(@PathVariable Long userId) {
		return ResponseEntity.ok(service.findByUser(userId).stream().map(serviceMapper::toDTO).toList());
	}

	/**
	 * Gets the by location.
	 *
	 * @param locationId the location id
	 * @return the by location
	 */
	@GetMapping("/location/{locationId}")
	public ResponseEntity<List<ServiceDTO>> getByLocation(@PathVariable Long locationId) {
		return ResponseEntity.ok(service.findByLocation(locationId).stream().map(serviceMapper::toDTO).toList());
	}

	@GetMapping("/{serviceId}")
	public ResponseEntity<ServiceDTO> getById(@PathVariable Long serviceId) {
		return ResponseEntity.ok(serviceMapper.toDTO(service.findById(serviceId)));
	}

	/**
	 * Update.
	 *
	 * @param id  the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ServiceDTO>> update(@PathVariable Long id, @RequestBody ServiceDTO dto) {
		ServiceEntity updated = service.update(id, serviceMapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Service updated", serviceMapper.toDTO(updated)));
	}
}
