package hr.java.web.prosport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class CartDto {
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private Integer totalItems;

    public CartDto(List<CartItemDto> items) {
        this.items = items;
        this.totalAmount = items.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalItems = items.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }
}