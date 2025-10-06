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
@Table(name = "global_customers", schema = "waturnos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GlobalCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalCustomerId;

    private String name;
    private String phone;
    private String email;

    private LocalDateTime createdAt = LocalDateTime.now();
}