package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.waturnos.entity.Booking;
import java.util.List;

/**
 * The Interface BookingRepository.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Find by tenant id.
     *
     * @param tenantId the tenant id
     * @return the list
     */
    List<Booking> findByTenantTenantId(Long tenantId); 
    /**
     * Find by customer customer id.
     *
     * @param customerId the customer id
     * @return the list
     */
    List<Booking> findByCustomerCustomerId(Long customerId);
    
}