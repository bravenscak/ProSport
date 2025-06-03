package hr.java.web.prosport.repository;

import hr.java.web.prosport.model.CartItem;
import hr.java.web.prosport.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findBySessionId(String sessionId);

    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProductId(User user, Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.sessionId IS NOT NULL AND c.user IS NULL")
    void deleteOrphanedSessionItems();
}