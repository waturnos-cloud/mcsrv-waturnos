package com.waturnos.dto;
import com.waturnos.enums.OrganizationStatus;
import lombok.Data;
@Data public class OrganizationDTO {
  private Long id; private String name; private String logoUrl; private String timezone; private String type;
  private String defaultLanguage; private Boolean active; private OrganizationStatus status;
}
