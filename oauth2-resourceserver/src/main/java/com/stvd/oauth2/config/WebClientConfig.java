package com.stvd.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;

@Configuration
public class WebClientConfig {

    @Bean
    BearerTokenAccessDeniedHandler accessDeniedHandler() {
        return new BearerTokenAccessDeniedHandler();
    }
}
