package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
	Optional<Client> findByEmailAndOrganizationId(String email, Long organizationId);

	List<Client> findByOrganizationId(Long organizationId);
}
