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

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.dto.beans.UnavailabilityDTO;
import com.waturnos.dto.request.CreateService;
import com.waturnos.dto.request.ValidateAvailabilityChangeRequest;
import com.waturnos.dto.response.AvailabilityImpactResponse;
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
	 * @param createService the create service
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ServiceDTO>> create(@RequestBody CreateService createService) {
		ServiceEntity created = service.create(serviceMapper.toEntity(createService.getServiceDto()),
				availabilityMapper.toEntityList(createService.getListAvailability()),
				createService.getServiceDto().getUser().getId(), createService.getServiceDto().getLocation().getId(),
				createService.isWorkInHollidays());
		
		// Guardar service props si existen
		if (createService.getServiceDto().getServiceProps() != null && !createService.getServiceDto().getServiceProps().isEmpty()) {
			((com.waturnos.service.impl.ServiceEntityServiceImpl) service).saveServiceProps(
				created.getId(), 
				createService.getServiceDto().getServiceProps()
			);
		}
		
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

	/**
	 * Gets the by id.
	 *
	 * @param serviceId the service id
	 * @return the by id
	 */
	@GetMapping("/{serviceId}")
	public ResponseEntity<ServiceDTO> getById(@PathVariable Long serviceId) {
		return ResponseEntity.ok(serviceMapper.toDTO(service.findById(serviceId)));
	}

	/**
	 * Update.
	 *
	 * @param serviceDto the service dto
	 * @return the response entity
	 */
	@PutMapping
	public ResponseEntity<ApiResponse<ServiceDTO>> update(@RequestBody ServiceDTO serviceDto) {
		ServiceEntity updated = service.update(serviceMapper.toEntity(serviceDto, true), serviceDto.getListAvailability());
		
		// Actualizar service props si existen
		if (serviceDto.getServiceProps() != null) {
			((com.waturnos.service.impl.ServiceEntityServiceImpl) service).saveServiceProps(
				updated.getId(), 
				serviceDto.getServiceProps()
			);
		}
		
		return ResponseEntity.ok(new ApiResponse<>(true, "Service updated", serviceMapper.toDTO(updated)));
	}

	@DeleteMapping("/{serviceId}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long serviceId) {
		service.delete(serviceId);
		return ResponseEntity.ok(new ApiResponse<>(true, "User deleted", null));
	}

	/**
	 * Lock calendar.
	 *
	 * @param unavailableDto the unavailable dto
	 * @return the response entity
	 */
	@PostMapping("/calendar/lock")
	public ResponseEntity<ApiResponse<Void>> lockCalendar(@RequestBody UnavailabilityDTO unavailableDto) {
		service.lockCalendar(unavailableDto.getStartTime(), unavailableDto.getEndTime(), unavailableDto.getServiceId());
		return ResponseEntity.ok(new ApiResponse<>(true, "Lock calendar", null));
	}
	
	/**
	 * Valida el impacto de cambios en availability sobre bookings existentes.
	 * Retorna una lista de turnos/clientes que se verían afectados.
	 *
	 * @param request los datos de validación (serviceId y nueva availability)
	 * @return el impacto con cantidad y lista de bookings afectados
	 */
	@PostMapping("/availability/validate")
	public ResponseEntity<ApiResponse<AvailabilityImpactResponse>> validateAvailabilityChange(
			@RequestBody ValidateAvailabilityChangeRequest request) {
		
		AvailabilityImpactResponse impact = service.validateAvailabilityChange(
				request.getServiceId(), 
				request.getNewAvailability());
		
		return ResponseEntity.ok(new ApiResponse<>(true, "Validation completed", impact));
	}
}
