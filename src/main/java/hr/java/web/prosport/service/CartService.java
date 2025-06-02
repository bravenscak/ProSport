package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.model.User;

public interface CartService {
    void addToCart(String sessionId, User user, Long productId, Integer quantity);
    void updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity);
    void removeFromCart(String sessionId, User user, Long cartItemId);
    CartDto getCart(String sessionId, User user);
    void clearCart(String sessionId, User user);
    void transferSessionCartToUser(String sessionId, User user);
    Integer getCartItemCount(String sessionId, User user);
}