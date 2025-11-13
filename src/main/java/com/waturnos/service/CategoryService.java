package com.waturnos.service;

import java.util.List;

import com.waturnos.dto.beans.CategoryTreeDTO;
import com.waturnos.entity.Category;

public interface CategoryService {

    List<Category> getParentCategories();

    List<Category> getChildCategories(Long parentId);

    Category getCategoryById(Long id);

    List<CategoryTreeDTO> getCategoryTree();
}