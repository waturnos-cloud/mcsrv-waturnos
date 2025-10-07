package com.waturnos.controller;
import com.waturnos.dto.OrganizationDTO;
import com.waturnos.entity.Organization;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/organizations")
public class OrganizationController {
  private final OrganizationService service; private final OrganizationMapper mapper;
  public OrganizationController(OrganizationService s, OrganizationMapper m){this.service=s; this.mapper=m;}
  @GetMapping public ResponseEntity<List<OrganizationDTO>> getAll(){ return ResponseEntity.ok(service.findAll().stream().map(mapper::toDto).toList()); }
  @GetMapping("/{id}") public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id){
    return service.findById(id).map(mapper::toDto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }
  @PostMapping public ResponseEntity<ApiResponse<OrganizationDTO>> create(@RequestBody OrganizationDTO dto){
    Organization created = service.create(mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Organization created", mapper.toDto(created)));
  }
  @PutMapping("/{id}") public ResponseEntity<ApiResponse<OrganizationDTO>> update(@PathVariable Long id, @RequestBody OrganizationDTO dto){
    Organization updated = service.update(id, mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Organization updated", mapper.toDto(updated)));
  }
  @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
    service.delete(id); return ResponseEntity.ok(new ApiResponse<>(true,"Organization deleted", null));
  }
}
