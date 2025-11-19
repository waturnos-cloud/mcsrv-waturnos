package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.ClientOrganization;

public interface ClientOrganizationRepository extends JpaRepository<ClientOrganization, Long> {
	
	
}
