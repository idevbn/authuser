package com.ead.authuser.configs.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImplement accessDeniedHandler;

    private static final String[] AUTH_WHITELIST = {
            "/auth/**"
    };

    public WebSecurityConfig(final UserDetailsServiceImpl userDetailsService,
                             final AuthenticationEntryPointImpl authenticationEntryPoint,
                             final AccessDeniedHandlerImplement accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public AuthenticationJwtFilter authenticationJwtFilter() {
        return new AuthenticationJwtFilter();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        final RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();

        final String hierarchy = "ROLE_ADMIN > ROLE_INSTRUCTOR \n ROLE_INSTRUCTOR > ROLE_STUDENT \n ROLE_STUDENT > ROLE_USER";

        roleHierarchy.setHierarchy(hierarchy);

        return roleHierarchy;
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler expressionHandler() {
        final DefaultMethodSecurityExpressionHandler expressionHandler
                = new DefaultMethodSecurityExpressionHandler();

        expressionHandler.setRoleHierarchy(this.roleHierarchy());

        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .accessDeniedHandler(this.accessDeniedHandler)
                .authenticationEntryPoint(this.authenticationEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable();

        http.addFilterBefore(
                this.authenticationJwtFilter(),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(
            final AuthenticationConfiguration authenticationConfig
    ) throws Exception {
        return authenticationConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
