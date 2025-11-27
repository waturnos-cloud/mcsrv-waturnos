package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    private final MessageSource messageSource;

    // Eventos disponibles por rol
    private static final List<String> ADMIN_EVENTS = List.of(
        "CLIENT_LIST_BY_ORG", "CLIENT_CREATE", "CLIENT_DELETE", "CLIENT_FIND_BY_ID",
        "CLIENT_FIND_BY_FIELDS", "CLIENT_ASSIGN_ORG", "CLIENT_UNASSIGN_ORG", "CLIENT_NOTIFY",
        "CLIENT_UPDATE", "CLIENT_UPCOMING_BOOKINGS", "ORG_CREATE", "ORG_UPDATE_BASIC",
        "ORG_UPDATE_LOCATIONS", "ORG_STATUS_CHANGE", "USER_CREATE_MANAGER", "USER_UPDATE_MANAGER",
        "USER_CREATE_PROVIDER", "USER_UPDATE_PROVIDER", "USER_DELETE_MANAGER", "USER_DELETE_PROVIDER",
        "USER_PROCESS_CREATE_MANAGER", "USER_PROCESS_CREATE_PROVIDER", "USER_PROCESS_UPDATE",
        "SERVICE_CREATE", "SERVICE_UPDATE", "SERVICE_DELETE", "SERVICE_LOCK_CALENDAR",
        "BOOKING_UPDATE_STATUS", "BOOKING_ASSIGN_CLIENT", "BOOKING_CANCEL"
    );

    private static final List<String> MANAGER_EVENTS = List.of(
        "CLIENT_LIST_BY_ORG", "CLIENT_CREATE", "CLIENT_DELETE", "CLIENT_FIND_BY_ID",
        "CLIENT_FIND_BY_FIELDS", "CLIENT_ASSIGN_ORG", "CLIENT_UNASSIGN_ORG", "CLIENT_NOTIFY",
        "CLIENT_UPDATE", "CLIENT_UPCOMING_BOOKINGS", "ORG_UPDATE_BASIC", "ORG_UPDATE_LOCATIONS",
        "USER_CREATE_PROVIDER", "USER_UPDATE_PROVIDER", "USER_DELETE_PROVIDER",
        "USER_PROCESS_CREATE_PROVIDER", "USER_PROCESS_UPDATE", "SERVICE_CREATE", "SERVICE_UPDATE",
        "SERVICE_DELETE", "SERVICE_LOCK_CALENDAR", "BOOKING_UPDATE_STATUS",
        "BOOKING_ASSIGN_CLIENT", "BOOKING_CANCEL"
    );

    private static final List<String> PROVIDER_EVENTS = List.of(
        "CLIENT_CREATE", "CLIENT_UPDATE", "CLIENT_NOTIFY", "CLIENT_UPCOMING_BOOKINGS",
        "SERVICE_UPDATE", "SERVICE_LOCK_CALENDAR", "BOOKING_UPDATE_STATUS",
        "BOOKING_ASSIGN_CLIENT", "BOOKING_CANCEL"
    );


    @Override
    public Page<Audit> getAuditsForCurrentUser(Long organizationId, LocalDateTime fromDate, 
                                                LocalDateTime toDate, String event, Long serviceId, Pageable pageable) {
        User currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return Page.empty(pageable);
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
                return auditRepository.findByOrganization(targetOrgId, fromDate, toDate, event, serviceId, pageable);

            case MANAGER:
                // MANAGER solo ve auditorías de su propia organización
                if (userOrgId == null) {
                    return Page.empty(pageable);
                }
                return auditRepository.findByOrganization(userOrgId, fromDate, toDate, event, serviceId, pageable);

            case PROVIDER:
                // PROVIDER solo ve auditorías relacionadas con sus servicios
                // Filtramos por email del provider para obtener solo sus acciones
                if (userOrgId == null || currentUser.getEmail() == null) {
                    return Page.empty(pageable);
                }
                return auditRepository.findByOrganizationAndEmail(
                    userOrgId, currentUser.getEmail(), fromDate, toDate, event, serviceId, pageable
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
            case ADMIN -> ADMIN_EVENTS;
            case MANAGER -> MANAGER_EVENTS;
            case PROVIDER -> PROVIDER_EVENTS;
            default -> List.of();
        };
        
        // Convertir cada código de evento a AuditEventDTO con su etiqueta
        return eventCodes.stream()
            .map(eventCode -> {
                String label;
                try {
                    label = messageSource.getMessage(
                        "audit.event." + eventCode, 
                        null,
                        LocaleContextHolder.getLocale()
                    );
                } catch (Exception e) {
                    // Si no existe la clave, usar el código como fallback
                    label = eventCode;
                }
                return AuditEventDTO.builder()
                    .event(eventCode)
                    .label(label)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
