package com.stvd.oauth2.client.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseController {

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    private Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();

    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };

    @Value("spring.security.oauth2.client.registration.client-password")
    protected ClientRegistration clientRegistration;

    public RestOperations restOperations;

    public BaseController() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;
    }

    public Map<String, Object> loadInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getPrincipal().toString();

        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(clientRegistration.getRegistrationId(), principalName);
        OAuth2UserRequest userRequest = getOAuth2UserRequest(oAuth2AuthorizedClient);
        RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
        ResponseEntity<Map<String, Object>> response = null;
        try {
            response = this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
        } catch (OAuth2AuthorizationException ex) {

        }

        //解析用户数据
        Map<String, Object> responseData = response.getBody();
        return responseData;
    }

    protected OAuth2UserRequest getOAuth2UserRequest(OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        Assert.notNull(oAuth2AuthorizedClient, "oAuth2AuthorizedClient cannot be null");
        ClientRegistration clientRegistration = oAuth2AuthorizedClient.getClientRegistration();
        OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        return oAuth2UserRequest;
    }
}
