package com.waturnos.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.response.AuditDTO;
import com.waturnos.dto.response.AuditEventDTO;
import com.waturnos.entity.Audit;
import com.waturnos.service.AuditQueryService;
import com.waturnos.mapper.AuditMapper;
import com.waturnos.mapper.AuditLabelResolver;

import lombok.RequiredArgsConstructor;

/**
 * Controller for audit log queries.
 * Access is role-based:
 * - ADMIN: can see all audits for any organization
 * - MANAGER: can see all audits for their organization
 * - PROVIDER: can see only their own service-related audits
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;
    private final AuditMapper auditMapper;
    private final AuditLabelResolver auditLabelResolver;

    /**
     * Get audit logs filtered by current user's role and permissions
     *
     * @param organizationId optional organization filter (ADMIN only)
     * @param fromDate optional start date
     * @param toDate optional end date
     * @param event optional event code filter
     * @param serviceId optional service filter
     * @param providerId optional provider filter
     * @param page page number (0-based)
     * @param size page size
     * @return page of audit entries
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<AuditDTO>>> getAudits(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> audits = auditQueryService.getAuditsForCurrentUser(organizationId, startDate, endDate, event, serviceId, providerId, pageable);
        Page<AuditDTO> dtoPage = audits.map(a -> auditMapper.toDto(a, auditLabelResolver));
        return ResponseEntity.ok(new ApiResponse<>(true, "Audits retrieved", dtoPage));
    }

    /**
     * Get available event codes with labels that the current user can query
     *
     * @return list of audit event DTOs with code and label
     */
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<AuditEventDTO>>> getAvailableEvents() {
        List<AuditEventDTO> events = auditQueryService.getAvailableEventsForCurrentUser();
        return ResponseEntity.ok(new ApiResponse<>(true, "Available events", events));
    }
}
