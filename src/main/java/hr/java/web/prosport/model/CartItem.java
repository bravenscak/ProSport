package hr.java.web.prosport.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtTime;

    public BigDecimal getTotalPrice() {
        return priceAtTime.multiply(BigDecimal.valueOf(quantity));
    }

    public CartItem(String sessionId, Product product, Integer quantity) {
        this.sessionId = sessionId;
        this.product = product;
        this.quantity = quantity;
        this.priceAtTime = product.getPrice();
    }

    public CartItem(User user, Product product, Integer quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.priceAtTime = product.getPrice();
    }
}