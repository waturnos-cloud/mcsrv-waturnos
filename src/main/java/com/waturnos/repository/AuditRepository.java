package com.waturnos.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.Audit;

/**
 * Repository for Audit entities with role-based filtering
 */
public interface AuditRepository extends JpaRepository<Audit, Long> {

    /**
     * Find audits by organization with optional date range, event and service filters
     */
    @Query(value = """
        SELECT * FROM audit a 
        WHERE a.organization_id = :organizationId
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
          AND (COALESCE(:serviceId, a.service_id, -1) = COALESCE(a.service_id, -1))
        ORDER BY a.event_date DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM audit a 
        WHERE a.organization_id = :organizationId
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
          AND (COALESCE(:serviceId, a.service_id, -1) = COALESCE(a.service_id, -1))
        """,
        nativeQuery = true)
    Page<Audit> findByOrganization(
        @Param("organizationId") Long organizationId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("event") String event,
        @Param("serviceId") Long serviceId,
        Pageable pageable
    );

    /**
     * Find audits by service (for providers to see their service-related audits)
     */
    @Query(value = """
        SELECT * FROM audit a 
        WHERE a.service_id = :serviceId
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
        ORDER BY a.event_date DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM audit a 
        WHERE a.service_id = :serviceId
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
        """,
        nativeQuery = true)
    Page<Audit> findByService(
        @Param("serviceId") Long serviceId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("event") String event,
        Pageable pageable
    );

    /**
     * Find audits by organization with email filter (for manager/provider filtering by user)
     */
    @Query(value = """
        SELECT * FROM audit a 
        WHERE a.organization_id = :organizationId
          AND a.email = :email
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
          AND (COALESCE(:serviceId, a.service_id, -1) = COALESCE(a.service_id, -1))
        ORDER BY a.event_date DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM audit a 
        WHERE a.organization_id = :organizationId
          AND a.email = :email
          AND (COALESCE(:fromDate, a.event_date) = a.event_date OR a.event_date >= :fromDate)
          AND (COALESCE(:toDate, a.event_date) = a.event_date OR a.event_date <= :toDate)
          AND (COALESCE(:event, a.event) = a.event)
          AND (COALESCE(:serviceId, a.service_id, -1) = COALESCE(a.service_id, -1))
        """,
        nativeQuery = true)
    Page<Audit> findByOrganizationAndEmail(
        @Param("organizationId") Long organizationId,
        @Param("email") String email,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("event") String event,
        @Param("serviceId") Long serviceId,
        Pageable pageable
    );
}

