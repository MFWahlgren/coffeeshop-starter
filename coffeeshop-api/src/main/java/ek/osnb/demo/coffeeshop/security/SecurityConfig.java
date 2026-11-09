package ek.osnb.demo.coffeeshop.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(CsrfConfigurer::spa)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/actuator/health").permitAll()
                        .requestMatchers("/api/register", "/api/login").permitAll()
                        .requestMatchers("/api/categories", "/api/products/**", "/api/carts/**").permitAll()

                        .requestMatchers("/api/me", "/api/assistant/chat").authenticated()

                        .requestMatchers("/api/customers/me").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders", "/api/orders/*/cancel").hasRole("CUSTOMER")

                        .requestMatchers("/api/orders/*").hasAnyRole("CUSTOMER", "STAFF")

                        .requestMatchers("/api/orders/*/ship", "/api/staff/**").hasRole("STAFF")

                        .anyRequest().denyAll()
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .failureHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );

        return http.build();
    }
}