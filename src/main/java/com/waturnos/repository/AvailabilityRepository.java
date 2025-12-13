package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.waturnos.entity.AvailabilityEntity;

public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {
	List<AvailabilityEntity> findByServiceId(Long serviceId);
	
	@Modifying
	void deleteByServiceId(Long serviceId);
}
