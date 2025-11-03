package com.waturnos.entity;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;
	private Long serviceId;
}
