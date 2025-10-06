package com.waturnos.controller;

import com.waturnos.entity.Tenant;
import com.waturnos.service.TenantService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<Tenant> getAll() {
        return tenantService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Tenant> getById(@PathVariable Long id) {
        return tenantService.findById(id);
    }

    @PostMapping
    public Tenant create(@RequestBody Tenant tenant) {
        return tenantService.save(tenant);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tenantService.delete(id);
    }
}