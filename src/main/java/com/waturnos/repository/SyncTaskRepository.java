package com.waturnos.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.SyncTask;
import com.waturnos.enums.ScheduleType;

public interface SyncTaskRepository extends JpaRepository<SyncTask, Long> {
    Optional<SyncTask> findByScheduleTypeAndLastExecutionDate(ScheduleType type, LocalDate date);
}
