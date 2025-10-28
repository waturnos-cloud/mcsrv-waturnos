package com.waturnos.dto.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {

	private Long id;
	private String name;
	private String address;
	private String phone;
	private String email;
	private Double latitude;
	private Double longitude;
	private Boolean active;
	@Builder.Default 
	private Boolean main = false;
}
