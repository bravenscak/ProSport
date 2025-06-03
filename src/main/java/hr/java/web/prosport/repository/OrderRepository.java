package hr.java.web.prosport.repository;

import hr.java.web.prosport.model.Order;
import hr.java.web.prosport.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByPaypalOrderId(String paypalOrderId);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserAndStatus(@Param("user") User user, @Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.user.username LIKE %:username% ORDER BY o.createdAt DESC")
    List<Order> findByUserUsernameContaining(@Param("username") String username);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.paymentMethod = :paymentMethod ORDER BY o.createdAt DESC")
    List<Order> findByPaymentMethod(@Param("paymentMethod") Order.PaymentMethod paymentMethod);

    @Query("SELECT o FROM Order o WHERE " +
            "(:username IS NULL OR o.user.username LIKE %:username%) AND " +
            "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByFilters(@Param("username") String username,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}