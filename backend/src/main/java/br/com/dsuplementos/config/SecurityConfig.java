package br.com.dsuplementos.config;

import br.com.dsuplementos.dto.comum.ApiErrorResponse;
import br.com.dsuplementos.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.time.Instant;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOrigins;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origins}") String allowedOrigins,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, error) -> escreverErro(
                                response,
                                HttpStatus.UNAUTHORIZED,
                                "AUTENTICACAO_NECESSARIA",
                                "Autenticacao necessaria ou expirada.",
                                request.getRequestURI()
                        ))
                        .accessDeniedHandler((request, response, error) -> escreverErro(
                                response,
                                HttpStatus.FORBIDDEN,
                                "ACESSO_NEGADO",
                                "Voce nao possui permissao para esta operacao.",
                                request.getRequestURI()
                        )))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cadastro", "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/produtos/**", "/api/marcas").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void escreverErro(
            HttpServletResponse response,
            HttpStatus status,
            String codigo,
            String mensagem,
            String caminho
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .erro(status.getReasonPhrase())
                .codigo(codigo)
                .mensagem(mensagem)
                .caminho(caminho)
                .campos(java.util.Map.of())
                .build());
    }
}
