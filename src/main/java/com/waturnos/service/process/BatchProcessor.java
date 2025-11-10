package com.waturnos.service.process;

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
	

}
