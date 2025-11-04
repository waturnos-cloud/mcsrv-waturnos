package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

	Optional<ServiceEntity> findByNameAndProviderOrganizationId(String name, Long providerOrganizationId);

	@Query("SELECT s FROM ServiceEntity s WHERE s.providerOrganization.provider.id = :providerId")
	List<ServiceEntity> findByProviderId(@Param("providerId") Long providerId);

	List<ServiceEntity> findByLocationId(Long locationId);

}
