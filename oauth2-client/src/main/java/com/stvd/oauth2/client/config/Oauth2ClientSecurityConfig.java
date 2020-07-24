package com.stvd.oauth2.client.config;

import com.stvd.oauth2.client.security.CustomOauth2AuthenticationProvider;
import com.stvd.oauth2.client.security.CustomOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

@Configuration
@EnableWebSecurity
public class Oauth2ClientSecurityConfig extends WebSecurityConfigurerAdapter {

    private final String clientRegistrationId = "client-password";

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
                .loginPage("/login")
                .failureUrl("/login?error=")
                .permitAll()
                .and()
                .oauth2Client();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        CustomOauth2AuthenticationProvider provider = new CustomOauth2AuthenticationProvider(this.clientRegistrationId);
        return provider;
    }

    @Bean
    OAuth2UserService oAuth2UserService() {
        CustomOauth2UserService userService = new CustomOauth2UserService();
        return userService;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }
}
