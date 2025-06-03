package hr.java.web.prosport.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener
@Component
@Slf4j
public class SessionCounterListener implements HttpSessionListener {

    private static final AtomicInteger activeSessions = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int active = activeSessions.incrementAndGet();
        se.getSession().setMaxInactiveInterval(60);

        log.info("NOVA SESIJA: {} (Aktivnih: {})",
                se.getSession().getId(), active);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            Object springContext = se.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
            if (springContext == null) {
                int active = activeSessions.decrementAndGet();
                log.info("SESIJA ZAVRŠENA: {} (Aktivnih: {})",
                        se.getSession().getId(), active);
            }
        } catch (Exception e) {
            int active = activeSessions.decrementAndGet();
            log.info("SESIJA EXPIRED: {} (Aktivnih: {})",
                    se.getSession().getId(), active);
        }
    }

    public static int getActiveSessionCount() {
        return activeSessions.get();
    }
}