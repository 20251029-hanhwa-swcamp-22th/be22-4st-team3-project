package com.mycompany._thstudy.category.command.domain.aggregate;

import jakarta.persistence.*;
/*import lombok.*;*/
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "default_categories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class DefaultCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CategoryType type;
}