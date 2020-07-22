package com.stvd.oauth2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.net.URI;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
    String introspectionUri;
    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
    String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
    String clientSecret;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest()
                                .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .opaqueToken(opaqueToken ->
                                        opaqueToken
                                                .introspector(tokenIntrospector())
                                )
                );
    }

    /**
     * OpaqueTokenIntrospector.introspect方法返回的数据格式
     *
     * Returns a JSON object representation of this token introspection
     * success response.
     *
     * <p>Example JSON object:
     *
     * <pre>
     * {
     *  "active"          : true,
     *  "client_id"       : "l238j323ds-23ij4",
     *  "username"        : "jdoe",
     *  "scope"           : "read write dolphin",
     *  "sub"             : "Z5O3upPC88QrAjx00dis",
     *  "aud"             : "https://protected.example.net/resource",
     *  "iss"             : "https://server.example.com/",
     *  "exp"             : 1419356238,
     *  "iat"             : 1419350238,
     *  "extension_field" : "twenty-seven"
     * }
     * </pre>
     */
    @Bean
    OpaqueTokenIntrospector tokenIntrospector() {
        NimbusOpaqueTokenIntrospector introspector = new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
        introspector.setRequestEntityConverter(defaultRequestEntityConverter(URI.create(introspectionUri)));
        return introspector;
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
        introspectionUri.getScheme();
        return token -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            return new RequestEntity<>(null, headers, HttpMethod.POST, introspectionUri);
        };
    }
}
