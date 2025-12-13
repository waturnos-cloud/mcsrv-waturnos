package com.waturnos.service.process;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;

import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.entity.ServiceEntity;

public interface BatchProcessor {
	
	
	/**
	 * Delete provider async.
	 *
	 * @param providerId the provider id
	 */
	void deleteProviderAsync(long providerId);

	/**
	 * Delete bookings async.
	 *
	 * @param serviceId the service id
	 */
	void deleteServiceAsync(long serviceId, String serviceName, boolean deleteService);
	
	/**
	 * Delete bookings.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param serviceEntity the service entity
	 */
	void deleteBookings(LocalDateTime startDate, LocalDateTime endDate, ServiceEntity serviceEntity);
	
	/**
	 * Procesa de forma asíncrona los bookings afectados por cambios en availability.
	 * Cancela los bookings y notifica a los clientes.
	 *
	 * @param serviceId el ID del servicio
	 * @param newAvailability la nueva configuración de availability
	 * @param authentication el contexto de autenticación del usuario
	 */
	void processAffectedBookingsAsync(Long serviceId, List<AvailabilityDTO> newAvailability, Authentication authentication);

}
