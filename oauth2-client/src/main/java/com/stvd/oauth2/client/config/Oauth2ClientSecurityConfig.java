package com.stvd.oauth2.client.config;

import com.stvd.oauth2.client.security.UserAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class Oauth2ClientSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/webjars/**");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorizeRequests ->
                authorizeRequests
                        .mvcMatchers("/", "/public/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin()
//                .loginPage("/login")
//                .failureUrl("/login?error=")
                .permitAll()
                .and()
                .oauth2Client();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider());
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    UserAuthenticationProvider customAuthenticationProvider() {
        UserAuthenticationProvider provider = new UserAuthenticationProvider();
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withUsername("admin")
                .username("admin")
                .password("$2a$10$y8YrNP/wR2hY1DK499M94.JlJnYf/b81.ASw7kZiCxHeLRO1FyFEu")
//                .password("{bcrypt}$2a$10$y8YrNP/wR2hY1DK499M94.JlJnYf/b81.ASw7kZiCxHeLRO1FyFEu")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
}
