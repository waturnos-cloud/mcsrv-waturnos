package com.waturnos.controller;
import com.waturnos.dto.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.mapper.ClientMapper;
import com.waturnos.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/clients")
public class ClientController {
  private final ClientService service; private final ClientMapper mapper;
  public ClientController(ClientService s, ClientMapper m){this.service=s; this.mapper=m;}
  @GetMapping("/organization/{orgId}") public ResponseEntity<List<ClientDTO>> getByOrganization(@PathVariable Long orgId){
    return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
  }
  @PostMapping public ResponseEntity<ApiResponse<ClientDTO>> create(@RequestBody ClientDTO dto){
    Client created = service.create(mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Client created", mapper.toDto(created)));
  }
  @PutMapping("/{id}") public ResponseEntity<ApiResponse<ClientDTO>> update(@PathVariable Long id, @RequestBody ClientDTO dto){
    Client updated = service.update(id, mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Client updated", mapper.toDto(updated)));
  }
  @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
    service.delete(id); return ResponseEntity.ok(new ApiResponse<>(true,"Client deleted", null));
  }
}
