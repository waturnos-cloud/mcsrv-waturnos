package com.waturnos.service;

import com.waturnos.entity.Tenant;
import com.waturnos.repository.TenantRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> findById(Long id) {
        return tenantRepository.findById(id);
    }

    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void delete(Long id) {
        tenantRepository.deleteById(id);
    }
    
    public Optional<Tenant> findByApiKey(String apiKey) {
        return Optional.ofNullable(tenantRepository.findByApiKey(apiKey));
    }
}