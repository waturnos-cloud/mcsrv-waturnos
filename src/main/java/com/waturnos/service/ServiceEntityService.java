package com.waturnos.service;

import com.waturnos.entity.ServiceEntity;
import com.waturnos.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServiceEntityService {
    private final ServiceRepository repo;

    public ServiceEntityService(ServiceRepository repo) {
        this.repo = repo;
    }

    public List<ServiceEntity> getByTenant(Long tenantId) {
        return repo.findByTenantTenantId(tenantId);
    }

    public ServiceEntity save(ServiceEntity service) {
        return repo.save(service);
    }
}