package com.ritchi.control_precios.config.security;

import com.ritchi.control_precios.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
              
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/index.xhtml"),
                    new AntPathRequestMatcher("/pages/login.xhtml"),
                    new AntPathRequestMatcher("/pages/403.xhtml"),
                    new AntPathRequestMatcher("/pages/404.xhtml"),
                    new AntPathRequestMatcher("/error"),
                    new AntPathRequestMatcher("/javax.faces.resource/**"),
                    new AntPathRequestMatcher("/resources/**"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/assets/**")
                ).permitAll()
                
                // Solo ADMIN
                .requestMatchers(
                    new AntPathRequestMatcher("/pages/create_users.xhtml")
                ).hasAuthority("ROLE_ADMIN")
                
                // ADMIN y USER
                .requestMatchers(
                    new AntPathRequestMatcher("/pages/home.xhtml"),
                    new AntPathRequestMatcher("/pages/products.xhtml"),
                    new AntPathRequestMatcher("/pages/clients.xhtml"),
                    new AntPathRequestMatcher("/pages/cotizaciones.xhtml") 
                ).hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                
                // Resto de rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/pages/login.xhtml")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/pages/home.xhtml", true)
                .failureUrl("/pages/login.xhtml?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/pages/login.xhtml?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/pages/403.xhtml")
            );

        return http.build();
    }
}