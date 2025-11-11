package com.ritchi.control_precios.config.security;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // metodo auxiliar para ccrear coincidencias de rutas con MVC
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    // crea los usuarios en memoria
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Crea un codificador de contraseñas
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // Crea un administrador de detalles de usuario en memoria
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        // Crea usuarios manualmente
        manager.createUser(User.builder()
                .username("admin")
                .password(encoder.encode("123")) // Contraseña codificada
                .authorities("ROLE_ADMIN") // Rol de admin
                .build());

        manager.createUser(User.builder()
                .username("user")
                .password(encoder.encode("123")) // Contraseña codificada
                .authorities("ROLE_USER") // Rol de usuario
                .build());

        // Devuelve el administrador de detalles de usuario con los usuarios definidos
        return manager;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
        try {
            http.csrf(AbstractHttpConfigurer::disable);
            http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(mvc.pattern("/login.xhtml")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/jakarta.faces.resource/**")).permitAll()
                    .anyRequest()
                    .authenticated())
                    .formLogin(formLogin -> formLogin
                            .loginPage("/login.xhtml").permitAll()
                            .failureUrl("/login.xhtml?error=true")
                            .defaultSuccessUrl("/home.xhtml"))
                    .logout(logout -> logout
                            .logoutSuccessUrl("/login.xhtml")
                            .deleteCookies("JSESSIONID"));
            return http.build();
        } catch (Exception ex) {
            throw new BeanCreationException("Wrong spring security configuration", ex);
        }
    }

}