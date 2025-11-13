package com.waturnos.service;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.ServiceEntity;
import java.util.List;

public interface ServiceEntityService {

	List<ServiceEntity> findByUser(Long userId);

	List<ServiceEntity> findByLocation(Long locationId);

	ServiceEntity findById(Long id);

	ServiceEntity create(ServiceEntity service, List<AvailabilityEntity> listAvailability, Long userId, Long locationId,
			boolean workInHollidays);

	ServiceEntity update(Long id, ServiceEntity service);
}
