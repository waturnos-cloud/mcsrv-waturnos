package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.ProviderOrganization;

@Repository
public interface ProviderOrganizationRepository extends JpaRepository<ProviderOrganization, Long> {
	List<ProviderOrganization> findByOrganizationId(Long organizationId);

	List<ProviderOrganization> findByProviderId(Long providerId);

	Optional<ProviderOrganization> findByProviderIdAndOrganizationId(Long providerId, Long organizationId);
}