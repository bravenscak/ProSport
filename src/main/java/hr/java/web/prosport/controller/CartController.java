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

    private final CartService cartService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session,
                           @AuthenticationPrincipal User user,
                           Model model) {
        String sessionId = session.getId();
        CartDto cart = cartService.getCart(sessionId, user);

        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam Long productId,
                                       @RequestParam(defaultValue = "1") Integer quantity,
                                       HttpSession session,
                                       @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.addToCart(sessionId, user, productId, quantity);

            Integer cartItemCount = cartService.getCartItemCount(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Proizvod je dodan u košaricu!",
                    "cartItemCount", cartItemCount
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@RequestParam Long cartItemId,
                                            @RequestParam Integer quantity,
                                            HttpSession session,
                                            @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.updateCartItemQuantity(sessionId, user, cartItemId, quantity);

            CartDto cart = cartService.getCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Količina je ažurirana!",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@RequestParam Long cartItemId,
                                            HttpSession session,
                                            @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.removeFromCart(sessionId, user, cartItemId);

            CartDto cart = cartService.getCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Proizvod je uklonjen iz košarice!",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(HttpSession session,
                                       @AuthenticationPrincipal User user) {
        try {
            String sessionId = session.getId();
            cartService.clearCart(sessionId, user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Košarica je očišćena!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
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