package com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Map;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    Page<T> dynamicSearchText(String searchText, Pageable pageable);

    Page<T> dynamicSearchFilters(Map<String, Object> filters, Pageable pageable);
}
