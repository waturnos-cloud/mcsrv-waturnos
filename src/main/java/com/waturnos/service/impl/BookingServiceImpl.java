package com.waturnos.service.impl;
import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.service.BookingService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    public BookingServiceImpl(BookingRepository bookingRepository){this.bookingRepository=bookingRepository;}
    @Override public List<Booking> findByOrganization(Long organizationId){return bookingRepository.findByOrganizationId(organizationId);}
    @Override public List<Booking> findByStatus(BookingStatus status){return bookingRepository.findByStatus(status);}
    @Override public Booking create(Booking booking){return bookingRepository.save(booking);}
    @Override public Booking updateStatus(Long id, BookingStatus status){
        Booking existing = bookingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        existing.setStatus(status); return bookingRepository.save(existing);
    }
}
