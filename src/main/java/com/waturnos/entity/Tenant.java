package com.waturnos.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tenants", schema = "waturnos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenantId;
    private String apiKey;
    private String name;
    private String whatsappNumber;
    private String email;
    private String address;
    private String timezone;

    private LocalDateTime createdAt = LocalDateTime.now();
}