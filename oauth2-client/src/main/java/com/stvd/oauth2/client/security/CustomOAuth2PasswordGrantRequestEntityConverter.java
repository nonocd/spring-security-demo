package com.stvd.oauth2.client.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

public class CustomOAuth2PasswordGrantRequestEntityConverter implements Converter<OAuth2PasswordGrantRequest, RequestEntity<?>> {

    private static HttpHeaders DEFAULT_TOKEN_REQUEST_HEADERS = getDefaultTokenRequestHeaders();

    /**
     * Returns the {@link RequestEntity} used for the Access Token Request.
     *
     * @param passwordGrantRequest the password grant request
     * @return the {@link RequestEntity} used for the Access Token Request
     */
    @Override
    public RequestEntity<?> convert(OAuth2PasswordGrantRequest passwordGrantRequest) {
        ClientRegistration clientRegistration = passwordGrantRequest.getClientRegistration();

        HttpHeaders headers = this.getTokenRequestHeaders(clientRegistration);
        MultiValueMap<String, String> formParameters = buildFormParameters(passwordGrantRequest);
        URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
                .build()
                .toUri();

        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
    }

    /**
     * Returns a {@link MultiValueMap} of the form parameters used for the Access Token Request body.
     *
     * @param passwordGrantRequest the password grant request
     * @return a {@link MultiValueMap} of the form parameters used for the Access Token Request body
     */
    private MultiValueMap<String, String> buildFormParameters(OAuth2PasswordGrantRequest passwordGrantRequest) {
        ClientRegistration clientRegistration = passwordGrantRequest.getClientRegistration();

        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, passwordGrantRequest.getGrantType().getValue());
        formParameters.add(OAuth2ParameterNames.USERNAME, passwordGrantRequest.getUsername());
        formParameters.add(OAuth2ParameterNames.PASSWORD, passwordGrantRequest.getPassword());
        if (!CollectionUtils.isEmpty(clientRegistration.getScopes())) {
            formParameters.add(OAuth2ParameterNames.SCOPE,
                    StringUtils.collectionToDelimitedString(clientRegistration.getScopes(), ","));
        }
        if (ClientAuthenticationMethod.POST.equals(clientRegistration.getClientAuthenticationMethod())) {
            formParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
            formParameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }

        return formParameters;
    }

    private HttpHeaders getTokenRequestHeaders(ClientRegistration clientRegistration) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(DEFAULT_TOKEN_REQUEST_HEADERS);
        if (ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
        }
        return headers;
    }

    private static HttpHeaders getDefaultTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final MediaType contentType = MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setContentType(contentType);
        return headers;
    }
}
