package com.jjdev.beehome_tecnico.shared.infrastructure.config;

import com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository",
        },
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class RepositoryConfig {
}
