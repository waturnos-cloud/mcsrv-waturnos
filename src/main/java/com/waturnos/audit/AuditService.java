package com.waturnos.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.waturnos.entity.Audit;
import com.waturnos.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Async
    public void save(Audit audit) {
        auditRepository.save(audit);
    }
}
