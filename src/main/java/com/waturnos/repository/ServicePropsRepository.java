package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.ServicePropsEntity;

public interface ServicePropsRepository extends JpaRepository<ServicePropsEntity, Long> {

    @Query("SELECT sp FROM ServicePropsEntity sp WHERE sp.service.id = :serviceId")
    List<ServicePropsEntity> findByServiceId(@Param("serviceId") Long serviceId);
    
    @Modifying
    @Query("DELETE FROM ServicePropsEntity sp WHERE sp.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);
}
