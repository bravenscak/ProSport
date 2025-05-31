package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.ProductDto;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductDto> findAll();
    List<ProductDto> findAvailable();
    Optional<ProductDto> findById(Long id);
    List<ProductDto> findByCategoryId(Long categoryId);
}