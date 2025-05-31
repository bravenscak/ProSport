package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDto> findAll();
    Optional<CategoryDto> findById(Long id);
    Optional<CategoryDto> findByName(String name);
}