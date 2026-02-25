package com.mycompany._thstudy.category.command.infrastructure.repository;

import com.mycompany._thstudy.category.command.domain.aggregate.DefaultCategory;
import com.mycompany._thstudy.category.command.domain.repository.DefaultCategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDefaultCategoryRepository
	extends JpaRepository<DefaultCategory, Long>, DefaultCategoryRepository {
}