package com.waturnos.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit", indexes = {
    @Index(name = "idx_audit_date", columnList = "event_date"),
    @Index(name = "idx_audit_org", columnList = "organization_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date")
    private LocalDateTime date;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "behavior")
    private String behavior;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "role")
    private String role; // ADMIN, MANAGER, PROVIDER

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "method_signature")
    private String methodSignature;

    // Duración de la ejecución en milisegundos
    @Column(name = "duration_ms")
    private Long durationMs;

    // IP origen (si disponible)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    // User-Agent del cliente (si disponible)
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    // Identificador de request/correlación
    @Column(name = "request_id", length = 64)
    private String requestId;
}
