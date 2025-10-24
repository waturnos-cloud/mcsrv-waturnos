package com.waturnos.repository;

import com.waturnos.entity.Organization;
import com.waturnos.enums.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	List<Organization> findByStatus(OrganizationStatus status);

	List<Organization> findByActiveTrue();
}
