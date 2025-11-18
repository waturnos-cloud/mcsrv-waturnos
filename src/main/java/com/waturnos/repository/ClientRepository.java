package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
	
	Optional<Client> findByEmailAndOrganizationId(String email, Long organizationId);

	List<Client> findByOrganizationId(Long organizationId);

	Optional<Client> findByEmail(String email);

	List<Client> findByEmailContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email,
			String phone, String name);

	@Query("SELECT DISTINCT c FROM Client c " +
		       "JOIN BookingClient bc ON bc.client = c " + 
		       "JOIN bc.booking b " +                      
		       "WHERE b.service.user.id = :providerId")
		List<Client> findByProviderId(@Param("providerId") Long providerId);
	
}
