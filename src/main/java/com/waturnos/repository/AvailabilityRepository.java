package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.AvailabilityEntity;

@Repository
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {
	
}
