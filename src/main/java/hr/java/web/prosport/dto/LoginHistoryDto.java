package hr.java.web.prosport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LoginHistoryDto {
    private Long id;
    private String username;
    private String ipAddress;
    private LocalDateTime loginTime;

    public LoginHistoryDto(String username, String ipAddress) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.loginTime = LocalDateTime.now();
    }
}