package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

	Optional<ServiceEntity> findByNameAndUserId(String name, Long userId);

	List<ServiceEntity> findByLocationId(Long locationId);
	
	List<ServiceEntity> findByUserId(Long userId);

}
