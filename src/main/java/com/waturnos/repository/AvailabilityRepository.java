package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.AvailabilityEntity;

public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {
	List<AvailabilityEntity> findByServiceId(Long serviceId);
}
