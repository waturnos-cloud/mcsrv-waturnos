package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for audit event with code and localized label
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {
    
    /**
     * Event code (e.g., "ORG_STATUS_CHANGE")
     */
    private String event;
    
    /**
     * Localized label from messages.properties (e.g., "Cambio de estado organización")
     */
    private String label;
}
