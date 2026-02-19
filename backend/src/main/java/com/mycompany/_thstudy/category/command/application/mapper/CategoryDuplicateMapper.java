package com.mycompany._thstudy.category.command.application.mapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryDuplicateMapper {

    boolean existsByUserEmailAndNameAndType(
        @Param("userEmail") String userEmail,
        @Param("name")      String name,
        @Param("type")      String type
    );

  boolean existsByUserEmailAndNameAndTypeExcludeId(
      @Param("userEmail") String userEmail,
      @Param("name") String name,
      @Param("type") String type,
      @Param("categoryId") Long categoryId);
}
