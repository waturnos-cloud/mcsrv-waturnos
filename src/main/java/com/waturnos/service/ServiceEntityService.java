package com.waturnos.service;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.ServiceEntity;
import java.util.List;

public interface ServiceEntityService {
	
	List<ServiceEntity> findByOrganizationProvider(Long organizationId, Long providerId);

	List<ServiceEntity> findByLocation(Long locationId);
	
	ServiceEntity findById(Long id);

	ServiceEntity create(ServiceEntity service, List<AvailabilityEntity> listAvailability, Long providerId, Long organizationId, Long locationId);

	ServiceEntity update(Long id, ServiceEntity service);
}
