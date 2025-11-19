package com.waturnos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.waturnos.dto.beans.CategoryTreeDTO;
import com.waturnos.entity.Category;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.CategoryService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CategoryService categoryService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	private Category testCategory;

	@BeforeEach
	void setUp() {
		testCategory = Category.builder()
				.id(1L)
				.name("Test Category")
				.active(true)
				.children(new ArrayList<>())
				.build();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener todas las categorías padre")
	void testGetParentCategories_Success() throws Exception {
		// Arrange
		List<Category> categories = List.of(testCategory);
		when(categoryService.getParentCategories()).thenReturn(categories);

		// Act & Assert
		mockMvc.perform(get("/categories")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener subcategorías de una categoría padre")
	void testGetChildCategories_Success() throws Exception {
		// Arrange
		Category child = Category.builder()
				.id(2L)
				.name("Child Category")
				.parent(testCategory)
				.active(true)
				.build();
		
		List<Category> children = List.of(child);
		when(categoryService.getChildCategories(1L)).thenReturn(children);

		// Act & Assert
		mockMvc.perform(get("/categories/1/children")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener árbol completo de categorías")
	void testGetCategoryTree_Success() throws Exception {
		// Arrange
		List<CategoryTreeDTO> treeResult = new ArrayList<>();
		when(categoryService.getCategoryTree()).thenReturn(treeResult);

		// Act & Assert
		mockMvc.perform(get("/categories/tree")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}
