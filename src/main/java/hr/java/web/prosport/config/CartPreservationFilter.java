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

    private final CartService cartService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("/login".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            String oldSessionId = request.getSession().getId();
            Integer cartCount = cartService.getCartItemCount(oldSessionId, null);

            request.setAttribute("oldSessionId", oldSessionId);
        }

        filterChain.doFilter(request, response);
    }
}