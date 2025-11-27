package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.waturnos.config.AuditEventsProperties;
import com.waturnos.mapper.AuditLabelResolver;
import com.waturnos.dto.response.AuditEventDTO;
import com.waturnos.entity.Audit;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.AuditRepository;
import com.waturnos.service.AuditQueryService;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditRepository auditRepository;
    private final AuditEventsProperties auditEvents;
    private final AuditLabelResolver auditLabelResolver;

    // Eventos se cargan desde application.yml vía AuditEventsProperties


    @Override
    public Page<Audit> getAuditsForCurrentUser(Long organizationId, LocalDateTime fromDate, 
                                                LocalDateTime toDate, String event, Long serviceId, 
                                                Long providerId, Pageable pageable) {
        User currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return Page.empty(pageable);
        }

        // Ajustar toDate a final del día (23:59:59.999999999) si viene con hora 00:00:00
        LocalDateTime adjustedToDate = toDate;
        if (toDate != null && toDate.toLocalTime().equals(java.time.LocalTime.MIN)) {
            adjustedToDate = toDate.toLocalDate().atTime(23, 59, 59, 999999999);
        }

        UserRole role = currentUser.getRole();
        Long userOrgId = SessionUtil.getOrganizationId();

        switch (role) {
            case ADMIN:
                // ADMIN puede ver auditorías de cualquier organización
                Long targetOrgId = organizationId != null ? organizationId : userOrgId;
                if (targetOrgId == null) {
                    return Page.empty(pageable);
                }
                return auditRepository.findByOrganization(targetOrgId, fromDate, adjustedToDate, event, serviceId, providerId, pageable);

            case MANAGER:
                // MANAGER solo ve auditorías de su propia organización
                if (userOrgId == null) {
                    return Page.empty(pageable);
                }
                return auditRepository.findByOrganization(userOrgId, fromDate, adjustedToDate, event, serviceId, providerId, pageable);

            case PROVIDER:
                // PROVIDER solo ve auditorías relacionadas con sus servicios
                // Filtramos por email del provider para obtener solo sus acciones
                if (userOrgId == null || currentUser.getEmail() == null) {
                    return Page.empty(pageable);
                }
                return auditRepository.findByOrganizationAndEmail(
                    userOrgId, currentUser.getEmail(), fromDate, adjustedToDate, event, serviceId, providerId, pageable
                );

            default:
                return Page.empty(pageable);
        }
    }

    @Override
    public List<AuditEventDTO> getAvailableEventsForCurrentUser() {
        User currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }

        UserRole role = currentUser.getRole();
        List<String> eventCodes = switch (role) {
            case ADMIN -> auditEvents.getAdmin();
            case MANAGER -> auditEvents.getManager();
            case PROVIDER -> auditEvents.getProvider();
            default -> List.of();
        };
        
        // Mapear a DTO resolviendo etiqueta con AuditLabelResolver y ordenar por label asc
        return eventCodes.stream()
            .map(code -> AuditEventDTO.builder()
                .event(code)
                .label(auditLabelResolver.labelFor(code))
                .build())
            .sorted(Comparator.comparing(AuditEventDTO::getLabel, Comparator.nullsLast(String::compareToIgnoreCase)))
            .collect(Collectors.toList());
    }
}
