package com.waturnos.service.impl;

import com.waturnos.entity.ServiceEntity;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.service.ServiceEntityService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServiceEntityServiceImpl implements ServiceEntityService {
	private final ServiceRepository serviceRepository;

	public ServiceEntityServiceImpl(ServiceRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

	@Override
	public List<ServiceEntity> findByProvider(Long providerId) {
		return serviceRepository.findByProviderId(providerId);
	}

	@Override
	public List<ServiceEntity> findByLocation(Long locationId) {
		return serviceRepository.findByLocationId(locationId);
	}

	@Override
	public ServiceEntity create(ServiceEntity service) {
		return serviceRepository.save(service);
	}

	@Override
	public ServiceEntity update(Long id, ServiceEntity service) {
		ServiceEntity existing = serviceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Service not found"));
		service.setId(existing.getId());
		return serviceRepository.save(service);
	}
}
