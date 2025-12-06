package com.waturnos.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Category - evita problemas de serialización JSON con relaciones lazy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
	private Long id;
	private String name;
	private boolean active;
	private Long parentId;
	private List<CategoryDTO> children;
}
