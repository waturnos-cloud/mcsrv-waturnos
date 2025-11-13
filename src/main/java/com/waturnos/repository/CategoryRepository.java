package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

}