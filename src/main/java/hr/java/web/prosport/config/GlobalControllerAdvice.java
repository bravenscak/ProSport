package hr.java.web.prosport.config;

import hr.java.web.prosport.model.User;
import hr.java.web.prosport.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;

    @ModelAttribute
    public void addCartItemCount(HttpSession session,
                                 @AuthenticationPrincipal User user,
                                 Model model) {
        try {
            String sessionId = session.getId();
            Integer cartItemCount = cartService.getCartItemCount(sessionId, user);
            model.addAttribute("cartItemCount", cartItemCount);
        } catch (Exception e) {
            model.addAttribute("cartItemCount", 0);
        }
    }
}