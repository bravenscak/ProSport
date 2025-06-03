package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.dto.CartItemDto;
import hr.java.web.prosport.model.CartItem;
import hr.java.web.prosport.model.Product;
import hr.java.web.prosport.model.User;
import hr.java.web.prosport.repository.CartItemRepository;
import hr.java.web.prosport.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import hr.java.web.prosport.exception.CartException;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private static final String PRODUCT_NOT_FOUND_MSG = "Proizvod nije pronađen";
    private static final String PRODUCT_NOT_AVAILABLE_MSG = "Proizvod nije dostupan";
    private static final String INVALID_QUANTITY_MSG = "Neispravna količina";
    private static final String INSUFFICIENT_STOCK_MSG = "Nema dovoljno proizvoda na zalihi";
    private static final String CART_ITEM_NOT_FOUND_MSG = "Stavka košarice nije pronađena";
    private static final String NO_PERMISSION_UPDATE_MSG = "Nemate dozvolu za ažuriranje ove stavke";
    private static final String NO_PERMISSION_REMOVE_MSG = "Nemate dozvolu za uklanjanje ove stavke";

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public void addToCart(String sessionId, User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CartException(PRODUCT_NOT_FOUND_MSG));

        if (!product.isAvailable()) {
            throw new CartException(PRODUCT_NOT_AVAILABLE_MSG);
        }

        if (quantity <= 0 || quantity > product.getStockQuantity()) {
            throw new CartException(INVALID_QUANTITY_MSG);
        }

        Optional<CartItem> existingItem;

        if (user != null) {
            existingItem = cartItemRepository.findByUserAndProductId(user, productId);
        } else {
            existingItem = cartItemRepository.findBySessionIdAndProductId(sessionId, productId);
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new CartException(INSUFFICIENT_STOCK_MSG);
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem;
            if (user != null) {
                newItem = new CartItem(user, product, quantity);
            } else {
                newItem = new CartItem(sessionId, product, quantity);
            }
            cartItemRepository.save(newItem);
        }
    }

    @Override
    public void updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException(CART_ITEM_NOT_FOUND_MSG));

        if ((user != null && !cartItem.getUser().equals(user)) ||
                (user == null && !sessionId.equals(cartItem.getSessionId()))) {
            throw new CartException(NO_PERMISSION_UPDATE_MSG);
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return;
        }

        if (quantity > cartItem.getProduct().getStockQuantity()) {
            throw new CartException(INSUFFICIENT_STOCK_MSG);
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeFromCart(String sessionId, User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException(CART_ITEM_NOT_FOUND_MSG));

        if ((user != null && !cartItem.getUser().equals(user)) ||
                (user == null && !sessionId.equals(cartItem.getSessionId()))) {
            throw new CartException(NO_PERMISSION_REMOVE_MSG);
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(String sessionId, User user) {
        List<CartItem> cartItems;

        if (user != null) {
            cartItems = cartItemRepository.findByUser(user);
        } else {
            cartItems = cartItemRepository.findBySessionId(sessionId);
        }

        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::mapToCartItemDto)
                .toList();

        return new CartDto(cartItemDtos);
    }

    @Override
    public void clearCart(String sessionId, User user) {
        if (user != null) {
            cartItemRepository.deleteByUser(user);
        } else {
            cartItemRepository.deleteBySessionId(sessionId);
        }
    }

    @Override
    public void transferSessionCartToUser(String sessionId, User user) {
        List<CartItem> sessionItems = cartItemRepository.findBySessionId(sessionId);

        for (CartItem sessionItem : sessionItems) {

            Optional<CartItem> existingUserItem = cartItemRepository
                    .findByUserAndProductId(user, sessionItem.getProduct().getId());

            if (existingUserItem.isPresent()) {
                CartItem userItem = existingUserItem.get();
                int newQuantity = Math.min(
                        userItem.getQuantity() + sessionItem.getQuantity(),
                        sessionItem.getProduct().getStockQuantity()
                );
                userItem.setQuantity(newQuantity);
                cartItemRepository.save(userItem);

                cartItemRepository.delete(sessionItem);
            } else {
                sessionItem.setUser(user);
                sessionItem.setSessionId(null);
                cartItemRepository.save(sessionItem);
            }
        }

        List<CartItem> remainingSessionItems = cartItemRepository.findBySessionId(sessionId);

        if (!remainingSessionItems.isEmpty()) {
            cartItemRepository.deleteBySessionId(sessionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount(String sessionId, User user) {
        List<CartItem> cartItems;

        if (user != null) {
            cartItems = cartItemRepository.findByUser(user);
        } else {
            cartItems = cartItemRepository.findBySessionId(sessionId);
        }

        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private CartItemDto mapToCartItemDto(CartItem cartItem) {
        Product product = cartItem.getProduct();

        return new CartItemDto(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getImageUrl(),
                product.getCategory().getName(),
                cartItem.getPriceAtTime(),
                cartItem.getQuantity(),
                product.getStockQuantity()
        );
    }
}