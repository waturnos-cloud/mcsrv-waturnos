package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.UnavailabilityEntity;

/**
 * The Interface UnavailabilityRepository.
 */
@Repository
public interface UnavailabilityRepository extends JpaRepository<UnavailabilityEntity, Long> {

	List<UnavailabilityEntity> findByServiceId(Long serviceId);
	
	List<UnavailabilityEntity> findByServiceIsNull();
}
