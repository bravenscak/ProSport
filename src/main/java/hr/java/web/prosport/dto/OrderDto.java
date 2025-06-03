package hr.java.web.prosport.dto;

import hr.java.web.prosport.model.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String orderNumber;
    private String paypalOrderId;
    private String paypalPaymentId;
    private Order.OrderStatus status;
    private Order.PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    private String userFullName;
    private String userEmail;

    private List<OrderItemDto> orderItems;
    private Integer totalItems;

    public OrderDto(String orderNumber, Order.PaymentMethod paymentMethod,
                    BigDecimal totalAmount, String shippingAddress, String phoneNumber) {
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.status = Order.OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}