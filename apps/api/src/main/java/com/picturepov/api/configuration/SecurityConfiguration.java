package com.picturepov.api.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
        // 401 and 403 go to GlobalExceptionHandler, so they have the same body as every other error.
        var bearer = new BearerTokenAuthenticationEntryPoint();
        AuthenticationEntryPoint entryPoint = (request, response, ex) -> {
            bearer.commence(request, response, ex);
            resolver.resolveException(request, response, null, ex);
        };
        AccessDeniedHandler accessDeniedHandler = (request, response, ex) ->
                resolver.resolveException(request, response, null, ex);

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers( "/actuator/health/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/*", "/events/*/quota").permitAll()
                        .requestMatchers(HttpMethod.POST, "/events/*/uploads").permitAll()
                        .requestMatchers("/admin/**").hasRole("admins")
                        .requestMatchers("/me/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    OAuth2TokenValidator<Jwt> cognitoAccessTokenValidator(@Value("${app.cognito.client-id}") String clientId) {
        return new DelegatingOAuth2TokenValidator<>(
                new JwtClaimValidator<String>("token_use", "access"::equals),
                new JwtClaimValidator<String>("client_id", clientId::equals));
    }
}
