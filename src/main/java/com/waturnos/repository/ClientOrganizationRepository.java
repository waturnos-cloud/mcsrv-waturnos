package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.waturnos.entity.Client;
import com.waturnos.entity.ClientOrganization;

/**
 * The Interface ClientOrganizationRepository.
 */
public interface ClientOrganizationRepository extends JpaRepository<ClientOrganization, Long> {
	

    /**
     * Find clients by organization.
     *
     * @param organizationId the organization id
     * @return the list
     */
    @Query("SELECT co.client FROM ClientOrganization co WHERE co.organization.id = :organizationId")
    List<Client> findClientsByOrganization(Long organizationId);
	
}
