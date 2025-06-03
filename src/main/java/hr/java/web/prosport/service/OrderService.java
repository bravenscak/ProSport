package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.OrderDto;
import hr.java.web.prosport.model.Order;
import hr.java.web.prosport.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    OrderDto createOrder(User user, Order.PaymentMethod paymentMethod, String shippingAddress, String phoneNumber, String notes);
    OrderDto updateOrderStatus(Long orderId, Order.OrderStatus status);
    OrderDto updatePayPalOrderId(Long orderId, String paypalOrderId);
    OrderDto confirmPayPalPayment(String paypalOrderId, String paypalPaymentId);
    Optional<OrderDto> findById(Long id);
    Optional<OrderDto> findByOrderNumber(String orderNumber);
    Optional<OrderDto> findByPayPalOrderId(String paypalOrderId);
    List<OrderDto> findByUser(User user);
    List<OrderDto> findAll();
    List<OrderDto> findByFilters(String username, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderDto> findByPaymentMethod(Order.PaymentMethod paymentMethod);
    Long countByStatus(Order.OrderStatus status);
    String generateOrderNumber();
}