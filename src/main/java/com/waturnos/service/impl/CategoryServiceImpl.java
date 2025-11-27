package com.waturnos.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.waturnos.dto.beans.CategoryTreeDTO;
import com.waturnos.entity.Category;
import com.waturnos.repository.CategoryRepository;
import com.waturnos.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 🟢 Devuelve todas las categorías que no tienen padre
     */
    @Override
    @Cacheable(cacheNames = "categories:parents")
    public List<Category> getParentCategories() {
        return categoryRepository.findByParentIsNull();
    }

    /**
     * 🟢 Devuelve categorías cuyo parent.id = parentId
     */
    @Override
    @Cacheable(cacheNames = "categories:children", key = "#parentId")
    public List<Category> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    /**
     * 🟢 Obtener categoría simple por id
     */
    @Override
    @Cacheable(cacheNames = "categories:byId", key = "#id")
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    /**
     * 🟢 Devuelve toda la estructura árbol padre → hijos
     */
    @Override
    @Cacheable(cacheNames = "categories:tree")
    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> parents = categoryRepository.findByParentIsNull();

        return parents.stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔄 Conversión recursiva de Category → CategoryTreeDTO
     */
    private CategoryTreeDTO convertToTreeDTO(Category category) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());

        List<CategoryTreeDTO> children = category.getChildren()
                .stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());

        dto.setChildren(children);
        return dto;
    }
}