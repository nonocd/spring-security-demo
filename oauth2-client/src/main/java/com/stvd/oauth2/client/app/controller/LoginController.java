package com.stvd.oauth2.client.app.controller;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Controller
public class LoginController {

    private String tokenUri = "http://localhost:9001/oauth/token";
    private String clientId = "";
    private String clientSecret = "";
    private String redirectUri = "http://localhost:8081/login/oauth2/code";

    @Autowired
    private RestTemplate restOperations;

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, String error) {
        model.addAttribute("loginError", error);
        return "login";
    }

    @GetMapping("/login/oauth2/code/{clientId}")
    public Object getAuthCode(@PathVariable String clientId, @RequestParam String code, String state) {

        String token = this.getToken(code);
        return String.format("clientId: %s, code: %s, state: %s, token: %s", clientId, code, state, token);
    }

    public String getToken(String code) {

        HttpHeaders headers = requestHeaders();
        MultiValueMap<String, String> body = requestBody(code);
        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(tokenUri));
        ResponseEntity<String> response = makeRequest(requestEntity);
        if (response.getStatusCodeValue() != HTTPResponse.SC_OK) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR, "token endpoint responded with " + response.getStatusCodeValue(), requestEntity.getUrl().toString()));
        }

        String token = response.getBody();
        return token;
    }

    private ResponseEntity<String> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return this.restOperations.exchange(requestEntity, String.class);
        } catch (Exception ex) {
            OAuth2Error oAuth2Error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED);
            throw new OAuth2AuthorizationException(oAuth2Error, ex.getMessage());
        }
    }

    private HttpHeaders requestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> requestBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        return body;
    }
}
