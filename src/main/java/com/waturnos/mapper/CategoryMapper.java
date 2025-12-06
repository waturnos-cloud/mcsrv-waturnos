package com.waturnos.mapper;

import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.response.CategoryDTO;
import com.waturnos.entity.Category;

/**
 * Mapper para Category - evita lazy loading issues.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

	/**
	 * Convierte Category entity a DTO sin cargar children.
	 *
	 * @param category la entidad
	 * @return el DTO
	 */
	@Mapping(target = "parentId", source = "parent.id")
	@Mapping(target = "children", ignore = true)
	CategoryDTO toDto(Category category);

	/**
	 * Convierte Category a DTO con children (si fueron cargados).
	 *
	 * @param category la entidad
	 * @return el DTO completo
	 */
	default CategoryDTO toDtoWithChildren(Category category) {
		CategoryDTO dto = toDto(category);
		
		// Solo mapear children si la colección fue inicializada (evita LazyInitializationException)
		if (category.getChildren() != null && !category.getChildren().isEmpty()) {
			dto.setChildren(
				category.getChildren().stream()
					.map(this::toDto)
					.collect(Collectors.toList())
			);
		}
		
		return dto;
	}
}
