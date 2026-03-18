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

/**
 * Configuración central de Spring Security.
 *
 * <p>Define tres aspectos principales:</p>
 * <ul>
 *   <li><b>Autenticación:</b> cómo se verifica la identidad (BCrypt + base de datos)</li>
 *   <li><b>Autorización:</b> qué URLs puede acceder cada rol</li>
 *   <li><b>Sesión:</b> comportamiento de login / logout</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Codificador de contraseñas BCrypt (factor de coste por defecto: 10).
     * Se usa al crear usuarios y al verificar credenciales en el login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación que combina:
     * <ul>
     *   <li>{@link CustomUserDetailsService} – carga el usuario desde la BD</li>
     *   <li>{@link BCryptPasswordEncoder} – verifica la contraseña hasheada</li>
     * </ul>
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Cadena de filtros de seguridad HTTP.
     *
     * <p>Orden de reglas (el primero que coincide gana):</p>
     * <ol>
     *   <li>Recursos públicos → {@code permitAll}</li>
     *   <li>Páginas de solo ADMIN → {@code hasAuthority("ROLE_ADMIN")}</li>
     *   <li>Páginas compartidas → {@code hasAnyAuthority(...)}</li>
     *   <li>Todo lo demás → debe estar autenticado</li>
     * </ol>
     *
     * <p><b>CSRF desactivado</b> porque JSF maneja su propia protección CSRF;
     * activar ambas causaría conflictos de tokens.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/",
                    "/index.xhtml",
                    "/pages/login.xhtml",
                    "/pages/403.xhtml",
                    "/pages/404.xhtml",
                    "/error",
                    "/javax.faces.resource/**",
                    "/resources/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/assets/**",
                    "/api/test/**"
                ).permitAll()

                .requestMatchers(
                    "/pages/create_users.xhtml",
                    "/pages/historial.xhtml"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                    "/pages/home.xhtml",
                    "/pages/products.xhtml"
                ).hasAnyAuthority("ROLE_ADMIN", "ROLE_COSTOS", "ROLE_CATALOGO", "ROLE_CONSULTA")

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
