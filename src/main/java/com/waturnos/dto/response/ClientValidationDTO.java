package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for client validation response.
 * Used to check if a client exists and is linked to an organization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientValidationDTO {
    
    /**
     * Whether the client exists in the database.
     */
    private boolean exists;
    
    /**
     * Whether the client is linked to the specified organization.
     */
    private boolean linked;
    
    /**
     * The client ID if exists, null otherwise.
     */
    private Long clientId;
    
    /**
     * Client data if exists, null otherwise.
     */
    private ClientDataDTO clientData;
    
    /**
     * Nested DTO for basic client data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientDataDTO {
        private String fullName;
        private String email;
        private String phone;
        private String dni;
    }
}
