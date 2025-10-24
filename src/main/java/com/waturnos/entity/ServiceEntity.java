package com.waturnos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private Integer durationMinutes;
	private Double price;
	private Integer advancePayment;
	private Integer futureDays;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private Provider provider;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Location location;
}
