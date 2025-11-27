package com.waturnos.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {
    private Long id;
    private LocalDateTime date;
    private String username;
    private String organizationName;
    private String providerName;
    private String serviceName;
    private Boolean success;
    private String ip; // maps from ipAddress
    private String object;
    private String eventLabel; // resolved from messages.properties
}
