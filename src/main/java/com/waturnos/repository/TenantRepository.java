package com.waturnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

	Tenant findByWhatsappNumber(String whatsappNumber);

	Tenant findByApiKey(String apiKey);
	
    Optional<Tenant> findByName(String name);

}