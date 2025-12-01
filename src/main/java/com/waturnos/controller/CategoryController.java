package com.waturnos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.waturnos.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	/**
	 * 🔹 Devuelve todas las categorías padre (parent = null) GET /categories.
	 *
	 * @return the parent categories
	 */
	@GetMapping
	public ResponseEntity<?> getParentCategories() {
		return ResponseEntity.ok(categoryService.getParentCategories());
	}
}