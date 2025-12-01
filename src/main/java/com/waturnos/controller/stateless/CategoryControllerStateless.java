package com.waturnos.controller.stateless;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.waturnos.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryControllerStateless {

	private final CategoryService categoryService;

	/**
	 * 🔹 Devuelve todas las categorías padre (parent = null) GET /categories
	 */
	@GetMapping
	public ResponseEntity<?> getParentCategories() {
		return ResponseEntity.ok(categoryService.getParentCategories());
	}

	/**
	 * 🔹 Devuelve todas las subcategorías de una categoría padre GET
	 * /categories/{parentId}/children
	 */
	@GetMapping("/{parentId}/children")
	public ResponseEntity<?> getChildCategories(@PathVariable Long parentId) {
		return ResponseEntity.ok(categoryService.getChildCategories(parentId));
	}

	/**
	 * 🔹 Devuelve todo el árbol de categorías GET /categories/tree
	 */
	@GetMapping("/tree")
	public ResponseEntity<?> getCategoryTree() {
		return ResponseEntity.ok(categoryService.getCategoryTree());
	}
}