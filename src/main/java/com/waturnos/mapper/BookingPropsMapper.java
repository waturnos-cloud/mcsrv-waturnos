package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.waturnos.dto.beans.BookingPropsDTO;
import com.waturnos.entity.BookingPropsEntity;

/**
 * The Interface BookingPropsMapper.
 */
@Mapper(componentModel = "spring")
public interface BookingPropsMapper {

	/**
	 * To dto.
	 *
	 * @param entity the entity
	 * @return the booking props DTO
	 */
	BookingPropsDTO toDto(BookingPropsEntity entity);

	/**
	 * To dto list.
	 *
	 * @param entities the entities
	 * @return the list
	 */
	List<BookingPropsDTO> toDtoList(List<BookingPropsEntity> entities);

	/**
	 * To entity.
	 *
	 * @param dto the dto
	 * @return the booking props entity
	 */
	BookingPropsEntity toEntity(BookingPropsDTO dto);

	/**
	 * To entity list.
	 *
	 * @param dtos the dtos
	 * @return the list
	 */
	List<BookingPropsEntity> toEntityList(List<BookingPropsDTO> dtos);
}
