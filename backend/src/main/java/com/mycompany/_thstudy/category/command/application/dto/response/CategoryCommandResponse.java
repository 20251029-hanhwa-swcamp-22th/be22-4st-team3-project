package com.mycompany._thstudy.category.command.application.dto.response;

import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CategoryCommandResponse {

    private Long id;
    private String name;
    private CategoryType type;
}
