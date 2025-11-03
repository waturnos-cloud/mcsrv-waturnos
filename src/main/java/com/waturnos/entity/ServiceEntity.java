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
@ToString(exclude = {"providerOrganizationId"})
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
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id")
	private ProviderOrganization providerOrganizationId;
}
