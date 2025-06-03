package hr.java.web.prosport.config;

import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.CartService;
import hr.java.web.prosport.service.LoginHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String OLD_SESSION_ID_ATTR = "oldSessionId";
    private static final String REDIRECT_HOME = "/";

    private final CartService cartService;
    private final LoginHistoryService loginHistoryService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal();
        String newSessionId = request.getSession().getId();

        String ipAddress = getClientIpAddress(request);
        loginHistoryService.recordLogin(user.getUsername(), ipAddress);

        try {
            Integer currentCartCount = cartService.getCartItemCount(newSessionId, null);
            String oldSessionId = (String) request.getAttribute(OLD_SESSION_ID_ATTR);
            if (oldSessionId != null && !oldSessionId.equals(newSessionId)) {
                Integer oldCartCount = cartService.getCartItemCount(oldSessionId, null);
                if (oldCartCount > 0) {
                    cartService.transferSessionCartToUser(oldSessionId, user);
                }
            } else {
                if (currentCartCount > 0) {
                    cartService.transferSessionCartToUser(newSessionId, user);
                }
            }
        } catch (Exception e) {
            log.warn("Cart transfer warning during login for user {}: {}", user.getUsername(), e.getMessage());
        }

        response.sendRedirect(REDIRECT_HOME);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}