package hr.java.web.prosport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productBrand;
    private String productImageUrl;
    private String categoryName;
    private BigDecimal priceAtTime;
    private Integer quantity;
    private Integer maxQuantity;
    private BigDecimal totalPrice;

    public CartItemDto(Long id, Long productId, String productName, String productBrand,
                       String productImageUrl, String categoryName, BigDecimal priceAtTime,
                       Integer quantity, Integer maxQuantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productImageUrl = productImageUrl;
        this.categoryName = categoryName;
        this.priceAtTime = priceAtTime;
        this.quantity = quantity;
        this.maxQuantity = maxQuantity;
        this.totalPrice = priceAtTime.multiply(BigDecimal.valueOf(quantity));
    }
}