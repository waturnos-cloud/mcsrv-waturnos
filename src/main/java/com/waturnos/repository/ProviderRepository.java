package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Provider;


public interface ProviderRepository extends JpaRepository<Provider, Long> {
	
}
