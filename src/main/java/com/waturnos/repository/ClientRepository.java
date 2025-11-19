package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
	

	Optional<Client> findByEmail(String email);

	@Query("SELECT c FROM Client c WHERE " + 
				"(:email IS NOT NULL AND c.email = :email) OR " + 
		       "(:phone IS NOT NULL AND c.phone = :phone) OR " + 
		       "(:dni IS NOT NULL AND c.dni = :dni)")
	Optional<Client> findByEmailOrPhoneOrDni(@Param("email") String email, @Param("phone") String phone,
			@Param("dni") String dni);
	
	@Query("SELECT DISTINCT c FROM Client c " +
		       "JOIN BookingClient bc ON bc.client = c " + 
		       "JOIN bc.booking b " +                      
		       "WHERE b.service.user.id = :providerId")
		List<Client> findByProviderId(@Param("providerId") Long providerId);

	
	@Query("SELECT c FROM Client c WHERE " +
	           "(:email IS NOT NULL AND c.email = :email) OR " + 
	           "(:dni IS NOT NULL AND c.dni = :dni) OR " +
	           "(:phone IS NOT NULL AND c.phone = :phone)")
	    Optional<Client> findExistingClientByUniqueFields(
	            @Param("email") String email,
	            @Param("dni") String dni,
	            @Param("phone") String phone);
	
}
