package com.waturnos.controller;

import com.waturnos.entity.ServiceEntity;
import com.waturnos.service.ServiceEntityService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceEntityController {

    private final ServiceEntityService serviceEntityService;

    public ServiceEntityController(ServiceEntityService serviceEntityService) {
        this.serviceEntityService = serviceEntityService;
    }

    @GetMapping("/tenant/{tenantId}")
    public List<ServiceEntity> getByTenant(@PathVariable Long tenantId) {
        return serviceEntityService.getByTenant(tenantId);
    }

    @PostMapping	
    public ServiceEntity create(@RequestBody ServiceEntity serviceEntity) {
        return serviceEntityService.save(serviceEntity);
    }
}