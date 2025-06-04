package hr.java.web.prosport.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class LoginAttemptListener {

    private static final AtomicInteger successfulLogins = new AtomicInteger(0);
    private static final AtomicInteger failedLogins = new AtomicInteger(0);

    @EventListener
    public void handleLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        int count = successfulLogins.incrementAndGet();
        log.info("Uspješna prijava: {} (Ukupno: {})", username, count);
    }

    @EventListener
    public void handleLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        int count = failedLogins.incrementAndGet();
        log.warn("Neuspješna prijava: {} (Ukupno: {})", username, count);
    }

    public static int getSuccessfulLogins() {
        return successfulLogins.get();
    }

    public static int getFailedLogins() {
        return failedLogins.get();
    }

}