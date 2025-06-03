package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.dto.OrderDto;
import hr.java.web.prosport.dto.OrderItemDto;
import hr.java.web.prosport.exception.OrderException;
import hr.java.web.prosport.model.*;
import hr.java.web.prosport.repository.OrderRepository;
import hr.java.web.prosport.repository.OrderItemRepository;
import hr.java.web.prosport.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String EMPTY_CART_MSG = "Cannot create order with empty cart";
    private static final String ORDER_NOT_FOUND_MSG = "Order not found";
    private static final String ORDER_NOT_FOUND_FOR_PAYPAL_MSG = "Order not found for PayPal ID: ";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found: ";
    private static final String INSUFFICIENT_STOCK_MSG = "Insufficient stock for product: ";
    private static final String ORDER_NUMBER_PREFIX = "PS-";
    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmmss";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    public OrderDto createOrder(User user, Order.PaymentMethod paymentMethod,
                                String shippingAddress, String phoneNumber, String notes) {

        log.info("Creating order for user: {}, payment method: {}", user.getUsername(), paymentMethod);

        CartDto cart = getCartForUser(user);

        if (cart.getItems().isEmpty()) {
            throw new OrderException(EMPTY_CART_MSG);
        }

        String orderNumber = generateOrderNumber();

        Order order = new Order(user, orderNumber, paymentMethod,
                cart.getTotalAmount(), shippingAddress, phoneNumber);
        order.setNotes(notes);
        order.setBillingAddress(shippingAddress);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = createOrderItem(savedOrder, cartItem);
                    return orderItemRepository.save(orderItem);
                })
                .toList();

        savedOrder.setOrderItems(orderItems);

        updateProductStock(cart);

        log.info("Order created successfully: {}", orderNumber);
        return mapToDTO(savedOrder);
    }

    public OrderDto createOrderWithSession(User user, String sessionId, Order.PaymentMethod paymentMethod,
                                           String shippingAddress, String phoneNumber, String notes) {

        log.info("Creating order for user: {}, payment method: {}", user.getUsername(), paymentMethod);

        CartDto cart = cartService.getCart(sessionId, user);

        if (cart.getItems().isEmpty()) {
            throw new OrderException(EMPTY_CART_MSG);
        }

        String orderNumber = generateOrderNumber();

        Order order = new Order(user, orderNumber, paymentMethod,
                cart.getTotalAmount(), shippingAddress, phoneNumber);
        order.setNotes(notes);
        order.setBillingAddress(shippingAddress);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = createOrderItem(savedOrder, cartItem);
                    return orderItemRepository.save(orderItem);
                })
                .toList();

        savedOrder.setOrderItems(orderItems);

        updateProductStock(cart);

        log.info("Order created successfully: {}", orderNumber);
        return mapToDTO(savedOrder);
    }

    @Override
    public OrderDto updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND_MSG));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);

        return mapToDTO(updatedOrder);
    }

    @Override
    public OrderDto updatePayPalOrderId(Long orderId, String paypalOrderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND_MSG));

        order.setPaypalOrderId(paypalOrderId);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("PayPal order ID {} set for order {}", paypalOrderId, order.getOrderNumber());

        return mapToDTO(updatedOrder);
    }

    @Override
    public OrderDto confirmPayPalPayment(String paypalOrderId, String paypalPaymentId) {
        Order order = orderRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND_FOR_PAYPAL_MSG + paypalOrderId));

        order.setPaypalPaymentId(paypalPaymentId);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("PayPal payment confirmed for order {}", order.getOrderNumber());

        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> findById(Long id) {
        return orderRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> findByPayPalOrderId(String paypalOrderId) {
        return orderRepository.findByPaypalOrderId(paypalOrderId).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByFilters(String username, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByFilters(username, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByPaymentMethod(Order.PaymentMethod paymentMethod) {
        return orderRepository.findByPaymentMethod(paymentMethod)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000);
        return ORDER_NUMBER_PREFIX + timestamp + "-" + String.format("%03d", randomSuffix);
    }

    private CartDto getCartForUser(User user) {
        return new CartDto(List.of());
    }

    private OrderItem createOrderItem(Order order, hr.java.web.prosport.dto.CartItemDto cartItem) {
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new OrderException(PRODUCT_NOT_FOUND_MSG + cartItem.getProductId()));

        return new OrderItem(order, product, cartItem.getQuantity(), cartItem.getPriceAtTime());
    }

    private void updateProductStock(CartDto cart) {
        for (hr.java.web.prosport.dto.CartItemDto item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new OrderException(PRODUCT_NOT_FOUND_MSG + item.getProductId()));

            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new OrderException(INSUFFICIENT_STOCK_MSG + product.getName());
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }

    private OrderDto mapToDTO(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPaypalOrderId(order.getPaypalOrderId());
        dto.setPaypalPaymentId(order.getPaypalPaymentId());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setNotes(order.getNotes());

        if (order.getUser() != null) {
            dto.setUserFullName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
            dto.setUserEmail(order.getUser().getEmail());
        }

        if (order.getOrderItems() != null) {
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(this::mapOrderItemToDTO)
                    .toList();
            dto.setOrderItems(itemDtos);
            dto.setTotalItems(itemDtos.stream().mapToInt(OrderItemDto::getQuantity).sum());
        }

        return dto;
    }

    private OrderItemDto mapOrderItemToDTO(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProductName());
        dto.setProductBrand(orderItem.getProductBrand());
        dto.setProductCategory(orderItem.getProductCategory());
        dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());

        return dto;
    }
}