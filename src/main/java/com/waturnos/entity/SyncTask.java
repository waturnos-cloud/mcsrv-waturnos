package com.waturnos.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.waturnos.enums.ExecutionStatus;
import com.waturnos.enums.ScheduleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sync_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 80)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExecutionStatus status;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "last_execution_date", nullable = false)
    private LocalDate lastExecutionDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
