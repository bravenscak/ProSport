package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.dto.OrderDto;
import hr.java.web.prosport.model.Order;
import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.CartService;
import hr.java.web.prosport.service.OrderService;
import hr.java.web.prosport.service.OrderServiceImpl;
import hr.java.web.prosport.service.PayPalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PayPalService payPalService;

    @GetMapping
    public String showCheckout(HttpSession session,
                               @AuthenticationPrincipal User user,
                               Model model) {

        if (user == null) {
            return "redirect:/login?message=Please login to continue with checkout";
        }

        String sessionId = session.getId();
        CartDto cart = cartService.getCart(sessionId, user);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart?error=Your cart is empty";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("paymentMethod") String paymentMethod,
                             @RequestParam("shippingAddress") String shippingAddress,
                             @RequestParam("phoneNumber") String phoneNumber,
                             @RequestParam(value = "notes", required = false) String notes,
                             HttpSession session,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes attributes) {

        try {
            if (user == null) {
                attributes.addFlashAttribute("error", "You must be logged in to place an order");
                return "redirect:/login";
            }

            String sessionId = session.getId();
            CartDto cart = cartService.getCart(sessionId, user);

            if (cart.getItems().isEmpty()) {
                attributes.addFlashAttribute("error", "Your cart is empty");
                return "redirect:/cart";
            }

            Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod.toUpperCase());

            OrderDto order = ((OrderServiceImpl) orderService).createOrderWithSession(
                    user, sessionId, method, shippingAddress, phoneNumber, notes);

            if (method == Order.PaymentMethod.PAYPAL) {
                try {
                    String paypalOrderId = payPalService.createOrder(cart, order.getOrderNumber());
                    orderService.updatePayPalOrderId(order.getId(), paypalOrderId);

                    cartService.clearCart(sessionId, user);

                    return "redirect:/checkout/paypal/approve/" + paypalOrderId;

                } catch (Exception e) {
                    log.error("PayPal order creation failed: {}", e.getMessage(), e);
                    attributes.addFlashAttribute("error", "PayPal payment failed. Please try again.");
                    return "redirect:/checkout";
                }

            } else {
                orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);
                cartService.clearCart(sessionId, user);

                attributes.addFlashAttribute("success",
                        "Order placed successfully! Order number: " + order.getOrderNumber());
                return "redirect:/orders/" + order.getId();
            }

        } catch (Exception e) {
            log.error("Order placement failed: {}", e.getMessage(), e);
            attributes.addFlashAttribute("error", "Failed to place order. Please try again.");
            return "redirect:/checkout";
        }
    }

    @GetMapping("/paypal/approve/{paypalOrderId}")
    public String approvePayPalPayment(@PathVariable String paypalOrderId) {
        try {
            com.paypal.orders.Order paypalOrder = payPalService.getOrder(paypalOrderId);

            String approvalUrl = paypalOrder.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .map(link -> link.href())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No approval URL found"));

            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            log.error("Failed to get PayPal approval URL: {}", e.getMessage(), e);
            return "redirect:/checkout?error=Payment failed";
        }
    }

    @GetMapping("/paypal/success")
    public String payPalSuccess(@RequestParam("token") String paypalOrderId,
                                @RequestParam("PayerID") String payerId,
                                RedirectAttributes attributes) {
        try {
            log.info("PayPal payment success callback - Order ID: {}, Payer ID: {}", paypalOrderId, payerId);

            PayPalService.CaptureResult captureResult = payPalService.captureOrder(paypalOrderId);

            OrderDto order = orderService.findByPayPalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for PayPal ID: " + paypalOrderId));

            orderService.confirmPayPalPayment(paypalOrderId, captureResult.getCaptureId());

            attributes.addFlashAttribute("success",
                    "Payment successful! Your order has been confirmed. Order number: " + order.getOrderNumber());

            return "redirect:/orders/" + order.getId();

        } catch (Exception e) {
            log.error("PayPal payment confirmation failed: {}", e.getMessage(), e);
            attributes.addFlashAttribute("error", "Payment verification failed. Please contact support.");
            return "redirect:/orders";
        }
    }

    @GetMapping("/paypal/cancel")
    public String payPalCancel(@RequestParam("token") String paypalOrderId,
                               RedirectAttributes attributes) {
        try {
            log.info("PayPal payment cancelled - Order ID: {}", paypalOrderId);

            OrderDto order = orderService.findByPayPalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for PayPal ID: " + paypalOrderId));

            orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CANCELLED);

            attributes.addFlashAttribute("error", "Payment was cancelled. Your order has been cancelled.");
            return "redirect:/cart";

        } catch (Exception e) {
            log.error("Error handling PayPal cancellation: {}", e.getMessage(), e);
            attributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/cart";
        }
    }

    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<?> validateCheckout(@RequestParam("shippingAddress") String shippingAddress,
                                              @RequestParam("phoneNumber") String phoneNumber,
                                              HttpSession session,
                                              @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "You must be logged in"
                ));
            }

            if (shippingAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Shipping address is required"
                ));
            }

            if (phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Phone number is required"
                ));
            }

            String sessionId = session.getId();
            CartDto cart = cartService.getCart(sessionId, user);

            if (cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Your cart is empty"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Validation successful"
            ));

        } catch (Exception e) {
            log.error("Checkout validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validation failed"
            ));
        }
    }
}