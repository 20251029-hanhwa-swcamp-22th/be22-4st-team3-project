package com.mycompany._thstudy.category.command.application.dto.request;

import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryUpdateRequest {

  @NotBlank
  @Size(max = 50)
  private String name;

}
