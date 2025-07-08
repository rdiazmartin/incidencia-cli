package com.example.incidenciacli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. Configura la autorización para TODAS las peticiones
                .authorizeHttpRequests(auth -> auth
                        // Permite el acceso público a la consola H2
                        .requestMatchers("/basedatos/**").permitAll()
                        // Reglas específicas para tu API
                        .requestMatchers("/api/incidencias").hasRole("PROFESIONAL")
                        .requestMatchers("/api/list", "/api/remove/**").hasRole("ADMIN")
                        // Cualquier otra petición debe ser autenticada
                        .anyRequest().authenticated()
                )
                // 2. Configura la autenticación básica
                .httpBasic(withDefaults())
                // 3. Deshabilita CSRF para la consola H2
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/basedatos/**")
                )
                // 4. Permite que la consola H2 se muestre en un frame
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails profesional = User.withDefaultPasswordEncoder()
                .username("profesional")
                .password("password")
                .roles("PROFESIONAL")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(profesional, admin);
    }
}