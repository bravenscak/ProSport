package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String CART_KEY = "cart";
    private static final String CART_ITEM_COUNT_KEY = "cartItemCount";
    private static final String PRODUCT_ADDED_MSG = "Proizvod je dodan u košaricu!";
    private static final String QUANTITY_UPDATED_MSG = "Količina je ažurirana!";
    private static final String PRODUCT_REMOVED_MSG = "Proizvod je uklonjen iz košarice!";
    private static final String CART_CLEARED_MSG = "Košarica je očišćena!";

    private final CartService cartService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session,
                           @AuthenticationPrincipal User user,
                           Model model) {
        String sessionId = session.getId();
        CartDto cart = cartService.getCart(sessionId, user);

        model.addAttribute(CART_KEY, cart);
        return "cart";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam Long productId,
                                                         @RequestParam(defaultValue = "1") Integer quantity,
                                                         HttpSession session,
                                                         @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.addToCart(sessionId, user, productId, quantity);

            Integer cartItemCount = cartService.getCartItemCount(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    MESSAGE_KEY, PRODUCT_ADDED_MSG,
                    CART_ITEM_COUNT_KEY, cartItemCount
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestParam Long cartItemId,
                                                              @RequestParam Integer quantity,
                                                              HttpSession session,
                                                              @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.updateCartItemQuantity(sessionId, user, cartItemId, quantity);

            CartDto cart = cartService.getCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    MESSAGE_KEY, QUANTITY_UPDATED_MSG,
                    CART_KEY, cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam Long cartItemId,
                                                              HttpSession session,
                                                              @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.removeFromCart(sessionId, user, cartItemId);

            CartDto cart = cartService.getCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    MESSAGE_KEY, PRODUCT_REMOVED_MSG,
                    CART_KEY, cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session,
                                                         @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.clearCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    MESSAGE_KEY, CART_CLEARED_MSG
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, e.getMessage()
            ));
        }
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartItemCount(HttpSession session,
                                                    @AuthenticationPrincipal User user) {
        String sessionId = session.getId();
        Integer count = cartService.getCartItemCount(sessionId, user);
        return ResponseEntity.ok(count);
    }
}