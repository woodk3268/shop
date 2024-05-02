package com.shop.config;


import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{



        http.formLogin((it)->it
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
        );

        http.logout((it)->it.logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/"));

        http.authorizeHttpRequests((req)-> {
            req
                    .requestMatchers(antMatcher("/")).permitAll()
                    .requestMatchers(antMatcher("/favicon.ico")).permitAll()
                    .requestMatchers(antMatcher("/members/**")).permitAll()
                    .requestMatchers(antMatcher("/item/**")).permitAll()
                    .requestMatchers(antMatcher("/images/**")).permitAll()
                    .requestMatchers(antMatcher("/node_modules/**")).permitAll()
                    .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                    .anyRequest().authenticated();
        });


        http.exceptionHandling((it)-> it.authenticationEntryPoint(new CustomAuthenticationEntryPoint("/members/login")));

        return http.build();
    }
    @Bean
    AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web-> web.ignoring()

                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
