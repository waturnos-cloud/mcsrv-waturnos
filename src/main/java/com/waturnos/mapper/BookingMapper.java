package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.response.BookingExtendedDTO;

import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.extended.BookingSummaryDetail;

/**
 * The Interface BookingMapper.
 */
@Mapper(componentModel = "spring")
public interface BookingMapper {

	/**
	 * To entity list.
	 *
	 * @param dtos the dtos
	 * @return the list
	 */
	List<Booking> toEntityList(List<BookingDTO> dtos);

	/**
	 * To dto list.
	 *
	 * @param entities the entities
	 * @return the list
	 */
	List<BookingDTO> toDtoList(List<Booking> entities);

	/**
	 * To extended DTO.
	 *
	 * @param source the source
	 * @return the booking extended DTO
	 */
	@Mappings({ @Mapping(source = "clientName", target = "clientName"), })
	BookingExtendedDTO toExtendedDTO(BookingSummaryDetail source);

	/**
	 * To extended DTO list.
	 *
	 * @param sourceList the source list
	 * @return the list
	 */
	List<BookingExtendedDTO> toExtendedDTOList(List<BookingSummaryDetail> sourceList);

	/**
	 * To dto.
	 *
	 * @param e the e
	 * @return the booking DTO
	 */
	@Mappings({
			@Mapping(target = "serviceId", source = "service.id") })
	BookingDTO toDto(Booking e);

	/**
	 * To entity.
	 *
	 * @param d the d
	 * @return the booking
	 */
	@Mappings({
			@Mapping(target = "service", source = "serviceId", qualifiedByName = "serviceFromId") })
	Booking toEntity(BookingDTO d);

	/**
	 * Client.
	 *
	 * @param id the id
	 * @return the client
	 */
	@Named("clientFromId")
	public static Client client(Long id) {
		if (id == null)
			return null;
		Client c = new Client();
		c.setId(id);
		return c;
	}

	/**
	 * Serv.
	 *
	 * @param id the id
	 * @return the service entity
	 */
	@Named("serviceFromId")
	public static ServiceEntity serv(Long id) {
		if (id == null)
			return null;
		ServiceEntity s = new ServiceEntity();
		s.setId(id);
		return s;
	}

	/**
	 * To extended DTO list from day.
	 *
	 * @param value the value
	 * @return the list
	 */
	List<BookingExtendedDTO> toExtendedDTOListFromDay(List<Booking> value);
}
