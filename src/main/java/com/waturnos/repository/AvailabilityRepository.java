package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.AvailabilityEntity;

public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {
	
}
