package com.waturnos.dto;
import lombok.Data;
@Data public class ServiceDTO {
  private Long id; private String name; private String description; private Integer durationMinutes; private Double price;
  private Integer advancePayment; private Integer futureDays; private Long providerId; private Long locationId;
}
