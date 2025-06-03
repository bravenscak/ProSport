package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.ProductDto;
import hr.java.web.prosport.exception.ProductException;
import hr.java.web.prosport.model.Category;
import hr.java.web.prosport.model.Product;
import hr.java.web.prosport.repository.CategoryRepository;
import hr.java.web.prosport.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Kategorija nije pronađena";
    private static final String PRODUCT_NOT_FOUND_MSG = "Proizvod nije pronađen";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<ProductDto> findAvailable() {
        return productRepository.findByAvailableTrue().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<ProductDto> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<ProductDto> searchProducts(String query) {
        return productRepository.findBySearchQuery(query).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ProductException(CATEGORY_NOT_FOUND_MSG));

        Product product = mapToEntity(productDto);
        product.setCategory(category);
        product.setAvailable(true);

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND_MSG));

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ProductException(CATEGORY_NOT_FOUND_MSG));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        existingProduct.setBrand(productDto.getBrand());
        existingProduct.setCategory(category);
        existingProduct.setAvailable(Boolean.TRUE.equals(productDto.getAvailable()));

        if (productDto.getImageUrl() != null) {
            existingProduct.setImageUrl(productDto.getImageUrl());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND_MSG));
        productRepository.delete(product);
    }

    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setBrand(dto.getBrand());
        product.setAvailable(Boolean.TRUE.equals(dto.getAvailable()));
        product.setImageUrl(dto.getImageUrl());
        return product;
    }

    private ProductDto mapToDTO(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setBrand(product.getBrand());
        dto.setAvailable(product.isAvailable());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }
}