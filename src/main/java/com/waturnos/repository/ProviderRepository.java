package com.waturnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Provider;


public interface ProviderRepository extends JpaRepository<Provider, Long> {
	
	Optional<Provider> findByUserId(Long userId);
	
	
}
