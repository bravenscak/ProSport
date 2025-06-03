package hr.java.web.prosport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductDto {
    private Long id;
    @NotBlank(message = "Naziv je obavezan")
    @Size(min = 2, max = 100, message = "Naziv mora imati između 2 i 100 znakova")
    private String name;
    private String description;
    @DecimalMin(value = "0.0", message = "Cijena mora biti pozitivna")
    private BigDecimal price;
    @Min(value = 0, message = "Količina ne može biti negativna")
    private Integer stockQuantity;
    private String brand;
    private String imageUrl;
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