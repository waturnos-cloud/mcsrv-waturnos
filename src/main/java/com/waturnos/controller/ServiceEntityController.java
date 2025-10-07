package com.waturnos.controller;
import com.waturnos.dto.ServiceDTO;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.mapper.ServiceMapper;
import com.waturnos.service.ServiceEntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/services")
public class ServiceEntityController {
  private final ServiceEntityService service; private final ServiceMapper mapper;
  public ServiceEntityController(ServiceEntityService s, ServiceMapper m){this.service=s; this.mapper=m;}
  @GetMapping("/provider/{providerId}") public ResponseEntity<List<ServiceDTO>> getByProvider(@PathVariable Long providerId){
    return ResponseEntity.ok(service.findByProvider(providerId).stream().map(mapper::toDto).toList());
  }
  @GetMapping("/location/{locationId}") public ResponseEntity<List<ServiceDTO>> getByLocation(@PathVariable Long locationId){
    return ResponseEntity.ok(service.findByLocation(locationId).stream().map(mapper::toDto).toList());
  }
  @PostMapping public ResponseEntity<ApiResponse<ServiceDTO>> create(@RequestBody ServiceDTO dto){
    ServiceEntity created = service.create(mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Service created", mapper.toDto(created)));
  }
  @PutMapping("/{id}") public ResponseEntity<ApiResponse<ServiceDTO>> update(@PathVariable Long id, @RequestBody ServiceDTO dto){
    ServiceEntity updated = service.update(id, mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Service updated", mapper.toDto(updated)));
  }
}
