package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Organization;
import com.waturnos.enums.OrganizationStatus;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	List<Organization> findByStatusOrderByNameAsc(OrganizationStatus status);

	List<Organization> findByActiveTrueOrderByNameAsc();
	
}
