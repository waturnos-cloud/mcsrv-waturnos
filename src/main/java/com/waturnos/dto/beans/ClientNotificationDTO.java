package com.waturnos.dto.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientNotificationDTO {
    private String language; 
    private String subject;  
    private String message;  
    private Long organizationId; 
}
