package com.waturnos.entity;

import com.waturnos.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"service","provider","organization","client"})
public class Booking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private OffsetDateTime startTime;
	private OffsetDateTime endTime;
	@Enumerated(EnumType.STRING)
	private BookingStatus status;
	private String notes;
	private String cancelReason;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id")
	private Client client;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private Provider provider;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id")
	private ServiceEntity service;
}
