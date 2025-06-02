package hr.java.web.prosport.config;

import hr.java.web.prosport.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CartPreservationFilter cartPreservationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/products/**", "/categories/**", "/search",
                                "/register", "/login", "/logout",
                                "/css/**", "/js/**", "/images/**", "/static/**", "/uploads/**",
                                "/favicon.ico", "/h2-console/**", "/cart/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout").permitAll()
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .userDetailsService(userService)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/admin/categories/quick"))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .addFilterBefore(cartPreservationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}