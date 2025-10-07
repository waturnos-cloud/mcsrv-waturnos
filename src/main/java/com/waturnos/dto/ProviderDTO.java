package com.waturnos.dto;
import lombok.Data;
@Data public class ProviderDTO {
  private Long id; private String fullName; private String email; private String phone; private Boolean active; private Long organizationId;
}
