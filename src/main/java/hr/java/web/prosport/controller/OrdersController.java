package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.OrderDto;
import hr.java.web.prosport.model.Order;
import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrdersController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String ORDERS_ATTR = "orders";
    private static final String ORDER_ATTR = "order";
    private static final String USERNAME_ATTR = "username";
    private static final String START_DATE_ATTR = "startDate";
    private static final String END_DATE_ATTR = "endDate";
    private static final String PAYMENT_METHOD_ATTR = "paymentMethod";
    private static final String TOTAL_ORDERS_ATTR = "totalOrders";
    private static final String PENDING_ORDERS_ATTR = "pendingOrders";
    private static final String CONFIRMED_ORDERS_ATTR = "confirmedOrders";
    private static final String PAYPAL_ORDERS_ATTR = "paypalOrders";
    private static final String COD_ORDERS_ATTR = "codOrders";
    private static final String LOGIN_MESSAGE = "?message=Please login to view your orders";
    private static final String ORDER_NOT_FOUND_MSG = "Order not found";
    private static final String ACCESS_DENIED_MSG = "Access denied";
    private static final String ADMIN_ROLE = "ADMIN";

    private final OrderService orderService;

    @GetMapping("/orders")
    public String userOrders(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return REDIRECT_LOGIN + LOGIN_MESSAGE;
        }

        List<OrderDto> orders = orderService.findByUser(user);
        model.addAttribute(ORDERS_ATTR, orders);
        return "user-orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        OrderDto order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));

        if (!user.getRole().name().equals(ADMIN_ROLE) && !order.getUserEmail().equals(user.getEmail())) {
            throw new RuntimeException(ACCESS_DENIED_MSG);
        }

        model.addAttribute(ORDER_ATTR, order);
        return "order-details";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(@RequestParam(required = false) String username,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                              @RequestParam(required = false) String paymentMethod,
                              Model model) {

        List<OrderDto> orders;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            orders = orderService.findByPaymentMethod(Order.PaymentMethod.valueOf(paymentMethod));
        } else {
            orders = orderService.findByFilters(username, startDateTime, endDateTime);
        }

        model.addAttribute(ORDERS_ATTR, orders);
        model.addAttribute(USERNAME_ATTR, username);
        model.addAttribute(START_DATE_ATTR, startDate);
        model.addAttribute(END_DATE_ATTR, endDate);
        model.addAttribute(PAYMENT_METHOD_ATTR, paymentMethod);

        model.addAttribute(TOTAL_ORDERS_ATTR, orders.size());
        model.addAttribute(PENDING_ORDERS_ATTR, orderService.countByStatus(Order.OrderStatus.PENDING));
        model.addAttribute(CONFIRMED_ORDERS_ATTR, orderService.countByStatus(Order.OrderStatus.CONFIRMED));
        model.addAttribute(PAYPAL_ORDERS_ATTR, orderService.findByPaymentMethod(Order.PaymentMethod.PAYPAL).size());
        model.addAttribute(COD_ORDERS_ATTR, orderService.findByPaymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY).size());

        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String adminOrderDetails(@PathVariable Long id, Model model) {
        OrderDto order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));

        model.addAttribute(ORDER_ATTR, order);
        return "admin/order-details";
    }
}