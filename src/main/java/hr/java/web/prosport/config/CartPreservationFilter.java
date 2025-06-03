package hr.java.web.prosport.config;

import hr.java.web.prosport.service.CartService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CartPreservationFilter extends OncePerRequestFilter {

    private static final String LOGIN_URI = "/login";
    private static final String POST_METHOD = "POST";
    private static final String OLD_SESSION_ID_ATTR = "oldSessionId";

    private final CartService cartService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (LOGIN_URI.equals(request.getRequestURI()) && POST_METHOD.equals(request.getMethod())) {
            String oldSessionId = request.getSession().getId();
            request.setAttribute(OLD_SESSION_ID_ATTR, oldSessionId);
        }

        filterChain.doFilter(request, response);
    }
}