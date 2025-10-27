package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.Provider;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
	
}
