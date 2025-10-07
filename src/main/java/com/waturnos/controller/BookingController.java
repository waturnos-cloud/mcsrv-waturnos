package com.waturnos.controller;
import com.waturnos.dto.BookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import com.waturnos.mapper.BookingMapper;
import com.waturnos.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/bookings")
public class BookingController {
  private final BookingService service; private final BookingMapper mapper;
  public BookingController(BookingService s, BookingMapper m){this.service=s; this.mapper=m;}
  @GetMapping("/organization/{orgId}") public ResponseEntity<List<BookingDTO>> getByOrganization(@PathVariable Long orgId){
    return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
  }
  @GetMapping("/status/{status}") public ResponseEntity<List<BookingDTO>> getByStatus(@PathVariable BookingStatus status){
    return ResponseEntity.ok(service.findByStatus(status).stream().map(mapper::toDto).toList());
  }
  @PostMapping public ResponseEntity<ApiResponse<BookingDTO>> create(@RequestBody BookingDTO dto){
    Booking created = service.create(mapper.toEntity(dto)); return ResponseEntity.ok(new ApiResponse<>(true,"Booking created", mapper.toDto(created)));
  }
  @PatchMapping("/{id}/status") public ResponseEntity<ApiResponse<BookingDTO>> updateStatus(@PathVariable Long id, @RequestParam BookingStatus status){
    Booking updated = service.updateStatus(id, status); return ResponseEntity.ok(new ApiResponse<>(true,"Booking status updated", mapper.toDto(updated)));
  }
}
