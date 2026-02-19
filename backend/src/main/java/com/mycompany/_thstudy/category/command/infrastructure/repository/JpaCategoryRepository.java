package com.mycompany._thstudy.category.command.infrastructure.repository;

import com.mycompany._thstudy.category.command.domain.aggregate.Category;
import com.mycompany._thstudy.category.command.domain.repository.CategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryRepository extends JpaRepository<Category, Long>, CategoryRepository {
}
