package com.sanosysalvos.bff_gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Mantiene tu configuración de CORS intacta
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilita CSRF (necesario para APIs REST sin estado)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Protege las rutas que el Gateway enviará a los microservicios
                        .requestMatchers("/api/bff/v1/mascotas/**").authenticated()
                        .requestMatchers("/api/bff/v1/geolocalizacion/**").authenticated()
                        // Protege las rutas del orquestador consolidado
                        .requestMatchers("/api/bff/v1/consolidado/**").authenticated()
                        
                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                // Activa la validación de JWT de Clerk basada en el application.properties
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Mantiene los orígenes que ya tenías configurados
        config.setAllowedOriginPatterns(List.of(
                "https://*.brs.devtunnels.ms",
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Permite explícitamente el header de Autorización para el Token JWT
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}