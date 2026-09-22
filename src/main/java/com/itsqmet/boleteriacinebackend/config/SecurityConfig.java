package com.itsqmet.boleteriacinebackend.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            HttpMethod.POST,
            "/api/auth/login",
            "/api/auth/registro"
          )
          .permitAll()
          .requestMatchers("/api/auth/perfil", "/api/auth/logout")
          .authenticated()
          .requestMatchers(HttpMethod.GET, "/api/funciones/**", "/api/salas/**")
          .permitAll()
          .requestMatchers("/api/funciones/**")
          .hasRole("ADMIN")
          .requestMatchers("/api/salas/**")
          .hasRole("ADMIN")
          .requestMatchers("/api/asientos/**")
          .hasRole("ADMIN")
          .requestMatchers("/api/usuarios/**")
          .hasRole("ADMIN")
          .requestMatchers("/api/compras/registrar", "/api/compras/mias")
          .hasAnyRole("ADMIN", "CLIENTE")
          .requestMatchers("/api/compras/**")
          .hasRole("ADMIN")
          .anyRequest()
          .authenticated()
      )
      .sessionManagement(session -> session.maximumSessions(1))
      .exceptionHandling(ex ->
        ex
          .authenticationEntryPoint((request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(401);
            response
              .getWriter()
              .write(
                "{\"error\": \"No autenticado. Debes hacer login primero.\"}"
              );
          })
          .accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(403);
            response
              .getWriter()
              .write(
                "{\"error\": \"No tienes permiso para realizar esta acción.\"}"
              );
          })
      );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(
      List.of(
        "http://localhost:4200",
        "http://127.0.0.1:4200",
        "http://192.168.137.1:4200",
        "http://192.168.137.*:4200"
      )
    );
    config.setAllowedMethods(
      List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
    );
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
