package com.waturnos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.SyncTask;
import com.waturnos.enums.ExecutionStatus;
import com.waturnos.enums.ScheduleType;
import com.waturnos.repository.SyncTaskRepository;
import com.waturnos.service.SyncTaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncTaskServiceImpl implements SyncTaskService {

    private final SyncTaskRepository repository;

    @Override
    public boolean wasExecutedOn(ScheduleType type, LocalDate date) {
        return repository.findByScheduleTypeAndLastExecutionDate(type, date).isPresent();
    }

    @Override
    @Transactional
    public void recordExecution(ScheduleType type, LocalDate date, ExecutionStatus status, String jsonDetails) {
        SyncTask task = repository.findByScheduleTypeAndLastExecutionDate(type, date).orElse(
            SyncTask.builder()
                .scheduleType(type)
                .lastExecutionDate(date)
                .build()
        );
        task.setStatus(status);
        task.setDetails(jsonDetails);
        task.setUpdatedAt(LocalDateTime.now());
        repository.save(task);
    }
}
