package com.spring.securitytutorial.config;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.security.registry.PermissionAuthorizationManager;
import com.spring.securitytutorial.util.security.ApiClientAuthenticationFilter;
import com.spring.securitytutorial.util.security.ApiClientAuthenticationProvider;
import com.spring.securitytutorial.util.security.AuthenticationChannelValidationFilter;
import com.spring.securitytutorial.util.security.RateLimitFilter;
import com.spring.securitytutorial.util.security.SessionValidationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.ArrayList;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder passwordEncoder,
                                                ApiClientAuthenticationProvider apiClients) {
        var dao = new DaoAuthenticationProvider(users);
        dao.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(dao, apiClients);
    }

    @Bean
    @Order(1)
    SecurityFilterChain actuator(HttpSecurity http) {
        return http.securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(o ->
                        o.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtConverter())))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain api(HttpSecurity http, SecurityProperties properties,
                            AuthenticationChannelValidationFilter channelFilter, ApiClientAuthenticationFilter clientFilter,
                            SessionValidationFilter sessionFilter,
                            ObjectProvider<RateLimitFilter> rateLimitFilterProvider,
                            PermissionAuthorizationManager permissionManager) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(_ -> {
                    var c = new org.springframework.web.cors.CorsConfiguration();
                    c.setAllowedOriginPatterns(properties.cors().allowedOriginPatterns());
                    c.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    c.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Tenant-Key",
                            "X-Tenant-Secret", "X-Current-User-Id", "X-Webhook-Key", "X-Webhook-Secret",
                            "X-Webhook-Timestamp", "X-Webhook-Signature"));
                    return c;
                }))
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true).preload(true).maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .permissionsPolicyHeader(permissions -> permissions.policy(
                                "camera=(), microphone=(), geolocation=()")))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/login", "/api/auth/refresh",
                                "/.well-known/jwks.json",
                                "/actuator/health", "/error").permitAll()
                        .requestMatchers("/api/webhooks/**").hasRole("WEBHOOK_CLIENT")
                        .requestMatchers("/api/users/**", "/api/roles/**", "/api/admin/**")
                        .access(permissionManager)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o ->
                        o.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtConverter())))
                .addFilterBefore(channelFilter, BearerTokenAuthenticationFilter.class)
                .addFilterBefore(clientFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(sessionFilter, BearerTokenAuthenticationFilter.class);

        rateLimitFilterProvider.ifAvailable(filter ->
        {
            try {
                http.addFilterBefore(filter, AuthenticationChannelValidationFilter.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return http.build();
    }

    private JwtAuthenticationConverter jwtConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new ArrayList<GrantedAuthority>();
            var roles = jwt.getClaimAsStringList("roles");
            if (roles != null) roles.forEach(value -> authorities.add(new SimpleGrantedAuthority(value)));
            var permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) permissions.forEach(value -> authorities.add(new SimpleGrantedAuthority(value)));
            return authorities;
        });
        return converter;
    }
}
