package com.mycompany._thstudy.category.query.mapper;

import com.mycompany._thstudy.category.query.dto.response.CategoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<CategoryResponse> findByUserEmail(@Param("userEmail") String userEmail);
}
