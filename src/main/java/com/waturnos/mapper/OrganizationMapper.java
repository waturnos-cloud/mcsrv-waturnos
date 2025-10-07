package com.waturnos.mapper;
import com.waturnos.dto.OrganizationDTO;
import com.waturnos.entity.Organization;
import org.mapstruct.Mapper;
@Mapper(componentModel="spring")
public interface OrganizationMapper {
    OrganizationDTO toDto(Organization entity);
    Organization toEntity(OrganizationDTO dto);
}
