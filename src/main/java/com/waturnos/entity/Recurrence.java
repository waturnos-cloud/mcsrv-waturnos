package com.waturnos.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.waturnos.enums.RecurrenceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "recurrence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"client", "service", "provider", "createdBy"})
public class Recurrence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurrence_seq_gen")
    @SequenceGenerator(name = "recurrence_seq_gen", sequenceName = "recurrence_id_seq", allocationSize = 1)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;
    
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Lunes, 7=Domingo
    
    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;
    
    @Column(name = "occurrence_count")
    private Integer occurrenceCount;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
