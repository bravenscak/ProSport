package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.LoginHistoryDto;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryService {
    void recordLogin(String username, String ipAddress);
    List<LoginHistoryDto> findAll();
    List<LoginHistoryDto> findByUsername(String username);
    List<LoginHistoryDto> findByFilters(String username, LocalDateTime startDate, LocalDateTime endDate);
    Long countRecentLogins(LocalDateTime since);
}
