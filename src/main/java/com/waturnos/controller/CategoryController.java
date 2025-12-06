package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.waturnos.dto.beans.CategoryTreeDTO;
import com.waturnos.dto.response.CategoryDTO;
import com.waturnos.mapper.CategoryMapper;
import com.waturnos.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/categories","/public/categories"})
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;
	private final CategoryMapper categoryMapper;

	/**
	 * 🔹 Devuelve todas las categorías padre (parent = null) GET /categories.
	 *
	 * @return the parent categories
	 */
	@GetMapping
	public ResponseEntity<List<CategoryDTO>> getParentCategories() {
		return ResponseEntity.ok(
			categoryService.getParentCategories().stream()
				.map(categoryMapper::toDto)
				.toList()
		);
	}

	/**
	 * 🔹 Devuelve las categorías hijas de una categoría padre.
	 *
	 * @param parentId el ID de la categoría padre
	 * @return the child categories
	 */
	@GetMapping("/{parentId}/children")
	public ResponseEntity<List<CategoryDTO>> getChildCategories(@PathVariable Long parentId) {
		return ResponseEntity.ok(
			categoryService.getChildCategories(parentId).stream()
				.map(categoryMapper::toDto)
				.toList()
		);
	}
	
	/**
	 * 🔹 Devuelve todo el árbol de categorías GET /categories/tree
	 */
	@GetMapping("/tree")
	public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {
		return ResponseEntity.ok(categoryService.getCategoryTree());
	}
}