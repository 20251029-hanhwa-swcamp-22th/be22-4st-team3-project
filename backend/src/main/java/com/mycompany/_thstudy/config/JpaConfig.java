package com.mycompany._thstudy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing 활성화 (createdAt, updatedAt 자동 관리)
}
