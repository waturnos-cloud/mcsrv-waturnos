package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.waturnos.dto.beans.LocationDTO;
import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.beans.CategoryTreeDTO;
import com.waturnos.entity.Location;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Category;

@Mapper(componentModel = "spring")
public abstract class OrganizationMapper {
	
	public OrganizationDTO toDto(Organization entity, boolean full) {
        OrganizationDTO dto = mapBasicInfo(entity);
        if (full) {
            // Llama al método abstracto para mapear el sub-bean
            dto.setLocations(mapLocations(entity.getLocations()));
        }
        return dto;
    }
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "type", source = "type", qualifiedByName = "categoryWithoutChildren")
    public abstract OrganizationDTO mapBasicInfo(Organization entity);
	
	public abstract List<LocationDTO> mapLocations(List<Location> subBean);
	
	public abstract List<Location> mapLocationsToEntity(List<LocationDTO> subBean);

    public abstract Organization toEntity(OrganizationDTO dto);

    @Named("categoryWithoutChildren")
    @Mapping(target = "children", ignore = true)
    protected abstract CategoryTreeDTO toCategoryTreeWithoutChildren(Category category);
}
