package com.mycompany._thstudy.category.command.domain.repository;

import com.mycompany._thstudy.category.command.domain.aggregate.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    void delete(Category category);
}
