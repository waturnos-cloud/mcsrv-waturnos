package com.waturnos.dto.beans;

import java.util.List;

import com.waturnos.enums.OrganizationStatus;
import lombok.Data;

@Data
public class OrganizationDTO {
	private Long id;
	private String name;
	private String logoUrl;
	private String timezone;
	private String type;
	private String defaultLanguage;
	private Boolean active;
	private OrganizationStatus status;
	private List<LocationDTO> locations;
	private boolean simpleOrganization;
}
