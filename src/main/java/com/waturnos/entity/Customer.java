package com.waturnos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "customers", schema = "waturnos",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "global_customer_id"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_customer_id")
    private GlobalCustomer globalCustomer;

    private LocalDateTime createdAt = LocalDateTime.now();
}