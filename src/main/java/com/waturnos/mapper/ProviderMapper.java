package com.waturnos.mapper;

import com.waturnos.dto.ProviderDTO;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProviderMapper {
	@Mapping(target = "organizationId", source = "organization.id")
	ProviderDTO toDto(Provider e);

	@Mapping(target = "organization", source = "organizationId", qualifiedByName = "orgFromId")
	Provider toEntity(ProviderDTO d);

	@Named("orgFromId")
	public static Organization mapOrg(Long id) {
		if (id == null)
			return null;
		Organization o = new Organization();
		o.setId(id);
		return o;
	}
}
