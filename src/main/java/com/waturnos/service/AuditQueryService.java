package com.waturnos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.waturnos.dto.response.AuditEventDTO;
import com.waturnos.entity.Audit;

/**
 * Service for querying audit logs with role-based access control
 */
public interface AuditQueryService {

    /**
     * Get audits filtered by current user's role and permissions.
     * - ADMIN: can see all audits for any organization
     * - MANAGER: can see all audits for their organization
     * - PROVIDER: can see only their own service-related audits
     * 
     * @param organizationId optional organization filter
     * @param fromDate optional start date
     * @param toDate optional end date
     * @param event optional event code filter
     * @param serviceId optional service filter
     * @param providerId optional provider filter
     * @param pageable pagination parameters
     * @return page of audit entries the current user can access
     */
    Page<Audit> getAuditsForCurrentUser(
        Long organizationId,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        String event,
        Long serviceId,
        Long providerId,
        Pageable pageable
    );

    /**
     * Get available event codes with labels that the current user can query.
     * Different roles see different events.
     * 
     * @return list of audit event DTOs with code and localized label
     */
    List<AuditEventDTO> getAvailableEventsForCurrentUser();
}
