package com.waturnos.service;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.ServiceEntity;

// TODO: Auto-generated Javadoc
/**
 * The Interface ServiceEntityService.
 */
public interface ServiceEntityService {

	/**
	 * Find by user.
	 *
	 * @param userId the user id
	 * @return the list
	 */
	List<ServiceEntity> findByUser(Long userId);

	/**
	 * Find by location.
	 *
	 * @param locationId the location id
	 * @return the list
	 */
	List<ServiceEntity> findByLocation(Long locationId);

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the service entity
	 */
	ServiceEntity findById(Long id);

	/**
	 * Creates the.
	 *
	 * @param service the service
	 * @param listAvailability the list availability
	 * @param userId the user id
	 * @param locationId the location id
	 * @param workInHollidays the work in hollidays
	 * @return the service entity
	 */
	ServiceEntity create(ServiceEntity service, List<AvailabilityEntity> listAvailability, Long userId, Long locationId,
			boolean workInHollidays);

	/**
	 * Update.
	 *
	 * @param service the service
	 * @return the service entity
	 */
	ServiceEntity update(ServiceEntity service);
	
	/**
	 * Delete.
	 *
	 * @param serviceId the service id
	 */
	void delete(Long serviceId);
	
	/**
	 * Lock calendar.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param serviceId the service id
	 */
	void lockCalendar(LocalDateTime startDate, LocalDateTime endDate, Long serviceId);
	
	
}
