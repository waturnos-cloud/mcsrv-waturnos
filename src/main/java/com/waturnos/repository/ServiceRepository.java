package com.waturnos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

	Optional<ServiceEntity> findByNameAndUserId(String name, Long userId);

	List<ServiceEntity> findByLocationId(Long locationId);
	
	@Query("SELECT s FROM ServiceEntity s WHERE s.user.id = :userId AND s.deleted = false")
	List<ServiceEntity> findByUserId(@Param("userId") Long userId);
	
	@Query("SELECT s FROM ServiceEntity s WHERE s.location.id = :locationId AND s.deleted = false")
	List<ServiceEntity> findActiveByLocationId(@Param("locationId") Long locationId);
	
	@Modifying
	@Query("UPDATE ServiceEntity s SET s.deleted = true WHERE s.id = :serviceId")
	void markAsDeleted(@Param("serviceId") Long serviceId);
	
	@Query("SELECT s FROM ServiceEntity s WHERE s.deleted = false")
	List<ServiceEntity> findAllActive();

	@Query("SELECT s FROM ServiceEntity s WHERE s.deleted = false")
	Page<ServiceEntity> findAllActive(Pageable pageable);
	
	@Modifying
	@Query("DELETE FROM ServiceEntity s WHERE s.user.id = :userId")
	void deleteAllByUserId(@Param("userId") Long userId);

}
