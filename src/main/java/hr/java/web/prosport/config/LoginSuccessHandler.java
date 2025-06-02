package hr.java.web.prosport.config;

import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final CartService cartService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal();
        String newSessionId = request.getSession().getId();

        try {
            Integer currentCartCount = cartService.getCartItemCount(newSessionId, null);

            String oldSessionId = (String) request.getAttribute("oldSessionId");
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
            System.err.println("Greška pri prebacivanju košarice: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect("/");
    }
}