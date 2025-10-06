package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.waturnos.entity.Booking;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTenantTenantId(Long tenantId);
    List<Booking> findByCustomerCustomerId(Long customerId);
}