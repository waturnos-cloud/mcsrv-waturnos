package com.waturnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Audit;

public interface AuditRepository extends JpaRepository<Audit, Long> {
}
