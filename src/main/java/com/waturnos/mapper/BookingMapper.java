package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.ServiceEntity;

@Mapper(componentModel = "spring")
public interface BookingMapper {

	List<Booking> toEntityList(List<BookingDTO> dtos);

	List<BookingDTO> toDtoList(List<Booking> entities);

	@Mappings({ @Mapping(target = "clientId", source = "client.id"),
			@Mapping(target = "serviceId", source = "service.id") })
	BookingDTO toDto(Booking e);

	@Mappings({ @Mapping(target = "client", source = "clientId", qualifiedByName = "clientFromId"),
			@Mapping(target = "service", source = "serviceId", qualifiedByName = "serviceFromId") })
	Booking toEntity(BookingDTO d);

	@Named("clientFromId")
	public static Client client(Long id) {
		if (id == null)
			return null;
		Client c = new Client();
		c.setId(id);
		return c;
	}

	@Named("serviceFromId")
	public static ServiceEntity serv(Long id) {
		if (id == null)
			return null;
		ServiceEntity s = new ServiceEntity();
		s.setId(id);
		return s;
	}
}
