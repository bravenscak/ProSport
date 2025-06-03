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

    private static final String ERROR_ATTR = "error";
    private static final String SUCCESS_ATTR = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String SUCCESS_KEY = "success";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_CART = "redirect:/cart";
    private static final String REDIRECT_CHECKOUT = "redirect:/checkout";
    private static final String REDIRECT_ORDERS = "redirect:/orders";
    private static final String LOGIN_MESSAGE = "Please login to continue with checkout";
    private static final String EMPTY_CART_MESSAGE = "Your cart is empty";
    private static final String LOGIN_REQUIRED_MESSAGE = "You must be logged in to place an order";
    private static final String PAYPAL_ERROR_MESSAGE = "PayPal payment failed. Please try again.";
    private static final String ORDER_FAILED_MESSAGE = "Failed to place order. Please try again.";
    private static final String PAYMENT_FAILED_MESSAGE = "Payment verification failed. Please contact support.";
    private static final String PAYMENT_CANCELLED_MESSAGE = "Payment was cancelled. Your order has been cancelled.";
    private static final String GENERAL_ERROR_MESSAGE = "An error occurred. Please try again.";
    private static final String LOGIN_REQUIRED_VALIDATION = "You must be logged in";
    private static final String ADDRESS_REQUIRED_MESSAGE = "Shipping address is required";
    private static final String PHONE_REQUIRED_MESSAGE = "Phone number is required";
    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
    private static final String VALIDATION_SUCCESS_MESSAGE = "Validation successful";

    private final CartService cartService;
    private final OrderService orderService;
    private final PayPalService payPalService;

    @GetMapping
    public String showCheckout(HttpSession session,
                               @AuthenticationPrincipal User user,
                               Model model) {

        if (user == null) {
            return REDIRECT_LOGIN + "?message=" + LOGIN_MESSAGE;
        }

        String sessionId = session.getId();
        CartDto cart = cartService.getCart(sessionId, user);

        if (cart.getItems().isEmpty()) {
            return REDIRECT_CART + "?" + ERROR_ATTR + "=" + EMPTY_CART_MESSAGE;
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
                attributes.addFlashAttribute(ERROR_ATTR, LOGIN_REQUIRED_MESSAGE);
                return REDIRECT_LOGIN;
            }

            String sessionId = session.getId();
            CartDto cart = cartService.getCart(sessionId, user);

            if (cart.getItems().isEmpty()) {
                attributes.addFlashAttribute(ERROR_ATTR, EMPTY_CART_MESSAGE);
                return REDIRECT_CART;
            }

            Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod.toUpperCase());

            OrderDto order = ((OrderServiceImpl) orderService).createOrderWithSession(
                    user, sessionId, method, shippingAddress, phoneNumber, notes);

            if (method == Order.PaymentMethod.PAYPAL) {
                return handlePayPalPayment(cart, order, sessionId, user, attributes);
            } else {
                return handleCashOnDeliveryPayment(order, sessionId, user, attributes);
            }

        } catch (Exception e) {
            log.error("Order placement failed: {}", e.getMessage(), e);
            attributes.addFlashAttribute(ERROR_ATTR, ORDER_FAILED_MESSAGE);
            return REDIRECT_CHECKOUT;
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
            return REDIRECT_CHECKOUT + "?" + ERROR_ATTR + "=Payment failed";
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

            attributes.addFlashAttribute(SUCCESS_ATTR,
                    "Payment successful! Your order has been confirmed. Order number: " + order.getOrderNumber());

            return "redirect:/orders/" + order.getId();

        } catch (Exception e) {
            log.error("PayPal payment confirmation failed: {}", e.getMessage(), e);
            attributes.addFlashAttribute(ERROR_ATTR, PAYMENT_FAILED_MESSAGE);
            return REDIRECT_ORDERS;
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

            attributes.addFlashAttribute(ERROR_ATTR, PAYMENT_CANCELLED_MESSAGE);
            return REDIRECT_CART;

        } catch (Exception e) {
            log.error("Error handling PayPal cancellation: {}", e.getMessage(), e);
            attributes.addFlashAttribute(ERROR_ATTR, GENERAL_ERROR_MESSAGE);
            return REDIRECT_CART;
        }
    }

    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCheckout(@RequestParam("shippingAddress") String shippingAddress,
                                                                @RequestParam("phoneNumber") String phoneNumber,
                                                                HttpSession session,
                                                                @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, LOGIN_REQUIRED_VALIDATION
                ));
            }

            if (shippingAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, ADDRESS_REQUIRED_MESSAGE
                ));
            }

            if (phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, PHONE_REQUIRED_MESSAGE
                ));
            }

            String sessionId = session.getId();
            CartDto cart = cartService.getCart(sessionId, user);

            if (cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, EMPTY_CART_MESSAGE
                ));
            }

            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    MESSAGE_KEY, VALIDATION_SUCCESS_MESSAGE
            ));

        } catch (Exception e) {
            log.error("Checkout validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, VALIDATION_FAILED_MESSAGE
            ));
        }
    }

    private String handlePayPalPayment(CartDto cart, OrderDto order, String sessionId, User user, RedirectAttributes attributes) {
        try {
            String paypalOrderId = payPalService.createOrder(cart, order.getOrderNumber());
            orderService.updatePayPalOrderId(order.getId(), paypalOrderId);
            cartService.clearCart(sessionId, user);
            return "redirect:/checkout/paypal/approve/" + paypalOrderId;
        } catch (Exception e) {
            log.error("PayPal order creation failed: {}", e.getMessage(), e);
            attributes.addFlashAttribute(ERROR_ATTR, PAYPAL_ERROR_MESSAGE);
            return REDIRECT_CHECKOUT;
        }
    }

    private String handleCashOnDeliveryPayment(OrderDto order, String sessionId, User user, RedirectAttributes attributes) {
        orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);
        cartService.clearCart(sessionId, user);
        attributes.addFlashAttribute(SUCCESS_ATTR,
                "Narudžba je uspješno kreirana! Broj narudžbe: " + order.getOrderNumber());
        return "redirect:/orders/" + order.getId();
    }
}