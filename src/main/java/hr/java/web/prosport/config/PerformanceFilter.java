package hr.java.web.prosport.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class PerformanceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("PerformanceFilter started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        if (uri.contains("/js/") || uri.contains("/css/") || uri.contains("/images/") ||
                uri.contains("/uploads/") || uri.contains("/cart/count") ||
                uri.contains(".well-known") || uri.contains("/favicon")) {
            chain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 300) {
                log.warn("SLOW: {} {} took {} ms", method, uri, duration);
            } else if (uri.startsWith("/admin")) {
                log.info("Admin: {} {} ({}ms)", method, uri, duration);
            } else if (uri.startsWith("/checkout") || uri.startsWith("/orders")) {
                log.info("Business: {} {} ({}ms)", method, uri, duration);
            }
        }
    }
}