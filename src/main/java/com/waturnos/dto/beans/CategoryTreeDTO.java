package com.waturnos.dto.beans;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTreeDTO {
    private Long id;
    private String name;
    private List<CategoryTreeDTO> children;
}