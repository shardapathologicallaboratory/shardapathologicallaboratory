package com.lcms.security;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final Environment environment;

    public SecurityConfig(CustomUserDetailsService userDetailsService, Environment environment) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isDev = environment.acceptsProfiles(Profiles.of("dev"));

        http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/login", "/v/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll();
                if (isDev) {
                    auth.requestMatchers("/h2-console/**").hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/h2-console/**").denyAll();
                }
                auth.requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/accountant/**").hasAnyRole("ADMIN", "ACCOUNTANT", "RECEPTION")
                    .requestMatchers("/reception/**").hasAnyRole("ADMIN", "RECEPTION", "ACCOUNTANT")
                    .requestMatchers("/collection/**").hasAnyRole("ADMIN", "COLLECTION")
                    .requestMatchers("/reporting/**").hasAnyRole("ADMIN", "REPORTING")
                    .requestMatchers("/technician/**").hasAnyRole("ADMIN", "TECHNICIAN", "REPORTING")
                    .requestMatchers("/reports/**").authenticated()
                    .anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        if (isDev) {
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.addHeaderWriter(
                    new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)
                ));
        }

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
