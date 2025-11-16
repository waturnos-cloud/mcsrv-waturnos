package com.waturnos.service.process;

import java.time.LocalDateTime;

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
	

}
