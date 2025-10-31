package com.waturnos.entity;

import java.time.LocalDateTime;

import com.waturnos.enums.BookingStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	@Enumerated(EnumType.STRING)
	private BookingStatus status;
	private String notes;
	private String cancelReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

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
