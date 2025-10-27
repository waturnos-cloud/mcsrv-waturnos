package com.waturnos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import com.waturnos.dto.beans.ProviderDTO;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;

@Mapper(componentModel = "spring")
public interface ProviderMapper {
	ProviderDTO toDto(Provider e);

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
