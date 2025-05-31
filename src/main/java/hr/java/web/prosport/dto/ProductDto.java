package hr.java.web.prosport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private Boolean available;

    public ProductDto(String name, String description, BigDecimal price, Integer stockQuantity, String brand, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.brand = brand;
        this.categoryId = categoryId;
    }
}