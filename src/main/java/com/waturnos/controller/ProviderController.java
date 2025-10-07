package com.waturnos.controller;
import com.waturnos.dto.ProviderDTO;
import com.waturnos.entity.Provider;
import com.waturnos.mapper.ProviderMapper;
import com.waturnos.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/providers")
public class ProviderController {
  private final ProviderService service; private final ProviderMapper mapper;
  public ProviderController(ProviderService s, ProviderMapper m){this.service=s; this.mapper=m;}
  @GetMapping("/organization/{orgId}") public ResponseEntity<List<ProviderDTO>> getByOrganization(@PathVariable Long orgId){
    return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
  }
  @PostMapping public ResponseEntity<ApiResponse<ProviderDTO>> create(@RequestBody ProviderDTO dto){
    Provider created = service.create(mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Provider created", mapper.toDto(created)));
  }
  @PutMapping("/{id}") public ResponseEntity<ApiResponse<ProviderDTO>> update(@PathVariable Long id, @RequestBody ProviderDTO dto){
    Provider updated = service.update(id, mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Provider updated", mapper.toDto(updated)));
  }
  @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
    service.delete(id); return ResponseEntity.ok(new ApiResponse<>(true,"Provider deleted", null));
  }
}
