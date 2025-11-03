package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
	List<ServiceEntity> findByProviderId(Long providerId);

	List<ServiceEntity> findByLocationId(Long locationId);
}
