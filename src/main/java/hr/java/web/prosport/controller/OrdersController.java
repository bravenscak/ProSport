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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String userOrders(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login?message=Please login to view your orders";
        }

        List<OrderDto> orders = orderService.findByUser(user);
        model.addAttribute("orders", orders);
        return "user-orders";  // Simplified path
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        OrderDto order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!user.getRole().name().equals("ADMIN") && !order.getUserEmail().equals(user.getEmail())) {
            throw new RuntimeException("Access denied");
        }

        model.addAttribute("order", order);
        return "order-details";  // Simplified path
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

        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("paymentMethod", paymentMethod);

        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", orderService.countByStatus(Order.OrderStatus.PENDING));
        model.addAttribute("confirmedOrders", orderService.countByStatus(Order.OrderStatus.CONFIRMED));
        model.addAttribute("paypalOrders", orderService.findByPaymentMethod(Order.PaymentMethod.PAYPAL).size());
        model.addAttribute("codOrders", orderService.findByPaymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY).size());

        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String adminOrderDetails(@PathVariable Long id, Model model) {
        OrderDto order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        return "admin/order-details";
    }
}