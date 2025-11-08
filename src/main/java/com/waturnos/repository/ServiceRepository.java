package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

	Optional<ServiceEntity> findByNameAndUserId(String name, Long userId);

	List<ServiceEntity> findByLocationId(Long locationId);
	
	List<ServiceEntity> findByUserId(Long userId);
	
	@Modifying
	@Query("DELETE FROM ServiceEntity s WHERE s.user.id = :userId")
	void deleteAllByUserId(@Param("userId") Long userId);

}
