package hr.java.web.prosport.repository;

import hr.java.web.prosport.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findAllByOrderByLoginTimeDesc();

    @Query("SELECT lh FROM LoginHistory lh WHERE " +
            "(:username IS NULL OR lh.username LIKE %:username%) AND " +
            "(:startDate IS NULL OR lh.loginTime >= :startDate) AND " +
            "(:endDate IS NULL OR lh.loginTime <= :endDate) " +
            "ORDER BY lh.loginTime DESC")
    List<LoginHistory> findByFilters(@Param("username") String username,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.loginTime >= :since")
    Long countRecentLogins(@Param("since") LocalDateTime since);
}