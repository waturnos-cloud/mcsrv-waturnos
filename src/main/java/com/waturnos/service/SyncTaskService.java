package com.waturnos.service;

import java.time.LocalDate;

import com.waturnos.enums.ExecutionStatus;
import com.waturnos.enums.ScheduleType;

public interface SyncTaskService {
    boolean wasExecutedOn(ScheduleType type, LocalDate date);
    void recordExecution(ScheduleType type, LocalDate date, ExecutionStatus status, String jsonDetails);
}
