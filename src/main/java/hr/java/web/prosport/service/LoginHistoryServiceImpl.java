package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.LoginHistoryDto;
import hr.java.web.prosport.model.LoginHistory;
import hr.java.web.prosport.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    public void recordLogin(String username, String ipAddress) {
        try {
            LoginHistory loginHistory = new LoginHistory(username, ipAddress);
            loginHistoryRepository.save(loginHistory);
        } catch (Exception e) {
            log.error("Failed to record login for user: {}", username, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> findAll() {
        return loginHistoryRepository.findAllByOrderByLoginTimeDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> findByUsername(String username) {
        return loginHistoryRepository.findByUsernameOrderByLoginTimeDesc(username)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> findByFilters(String username, LocalDateTime startDate, LocalDateTime endDate) {
        return loginHistoryRepository.findByFilters(username, startDate, endDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countRecentLogins(LocalDateTime since) {
        return loginHistoryRepository.countRecentLogins(since);
    }

    private LoginHistoryDto mapToDto(LoginHistory loginHistory) {
        LoginHistoryDto dto = new LoginHistoryDto();
        dto.setId(loginHistory.getId());
        dto.setUsername(loginHistory.getUsername());
        dto.setIpAddress(loginHistory.getIpAddress());
        dto.setLoginTime(loginHistory.getLoginTime());
        return dto;
    }
}