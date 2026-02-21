package com.mycompany._thstudy.category.command.domain.repository;

import com.mycompany._thstudy.category.command.domain.aggregate.DefaultCategory;

import java.util.List;

public interface DefaultCategoryRepository {

	List<DefaultCategory> findAll();
}