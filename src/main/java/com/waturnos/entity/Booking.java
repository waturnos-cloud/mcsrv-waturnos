package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.waturnos.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "service", "bookingClients","recurrence" })
@EqualsAndHashCode(exclude = { "bookingClients" })
public class Booking {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_sequence")
	@SequenceGenerator(name = "booking_sequence", sequenceName = "booking_id_seq", allocationSize = 100)
	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	@Enumerated(EnumType.STRING)
	private BookingStatus status;
	private String notes;
	private String cancelReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id")
	private ServiceEntity service;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recurrence_id")
	private Recurrence recurrence;

	@Column(nullable = false)
	private Integer freeSlots;

	@Builder.Default
	@Column(name = "is_overbooking")
	private Boolean isOverbooking = false;

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@Builder.Default
	private Set<BookingClient> bookingClients = new HashSet<>();

	
	/**
	 * Checks if is reservable status.
	 *
	 * @return true, if is reservable status
	 */
	private boolean isReservableStatus() {
	    return this.status == BookingStatus.FREE
	        || this.status == BookingStatus.FREE_AFTER_CANCEL
	        || this.status == BookingStatus.PARTIALLY_RESERVED;
	}
	
	/**
	 * Añade una inscripción de cliente, decrementa freeSlots y actualiza el estado
	 * solo si se llenó completamente el turno.
	 */
	public void addBookingClient(BookingClient bookingClient) {

	    if (this.freeSlots < 0) {
	        throw new IllegalStateException("No free slots available for booking " + this.id);
	    }

	    this.bookingClients.add(bookingClient);
	    this.freeSlots--;

	    if (bookingClient.getBooking() != this) {
	        bookingClient.setBooking(this);
	    }

	    if (!isReservableStatus()) {
	        return;
	    }

	    if (this.freeSlots == 0) {
	        this.status = (this.status == BookingStatus.FREE_AFTER_CANCEL)
	                ? BookingStatus.RESERVED_AFTER_CANCEL
	                : BookingStatus.RESERVED;
	    } else {
	        this.status = BookingStatus.PARTIALLY_RESERVED;
	    }
	}

	/**
	 * Elimina una inscripción de cliente, incrementa freeSlots y actualiza el
	 * estado solo si pasa de lleno a libre.
	 */
	public void removeBookingClient(BookingClient bookingClient, Integer serviceCapacity) {

		this.bookingClients.remove(bookingClient);
		this.freeSlots++;

		if (this.status != BookingStatus.CANCELLED && this.status != BookingStatus.COMPLETED) {
			if (this.freeSlots.equals(serviceCapacity)) {
				this.status = BookingStatus.FREE;
			} else {
				this.status = BookingStatus.PARTIALLY_RESERVED;
			}
		}
	}
}
