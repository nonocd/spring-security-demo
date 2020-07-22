package com.stvd.oauth2.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
public class AuthorizationController {

    @Value("${messages.base-uri}")
    private String messagesBaseUri;

    @Autowired
    private WebClient webClient;

    @GetMapping(value = "/authorize", params = "grant_type=authorization_code")
    public String[] authorization_code_grant(Model model) {
        String[] messages = retrieveMessages("messaging-client-auth-code");
//        model.addAttribute("messages", messages);
        return messages;
    }

    @GetMapping("/authorized")        // registered redirect_uri for authorization_code
    public String[] authorized(Model model) {
        String[] messages = retrieveMessages("messaging-client-auth-code");
        model.addAttribute("messages", messages);
        return messages;
    }

    @GetMapping(value = "/authorize", params = "grant_type=client_credentials")
    public String client_credentials_grant(Model model) {
        String[] messages = retrieveMessages("messaging-client-client-creds");
        model.addAttribute("messages", messages);
        return "index";
    }

    @GetMapping(value = "/authorize", params = "grant_type=password")
    public String[] password_grant(Model model) {
        String[] messages = retrieveMessages("messaging-client-password");
        model.addAttribute("messages", messages);
        return messages;
    }

    private String[] retrieveMessages(String clientRegistrationId) {
        return this.webClient
                .get()
                .uri(this.messagesBaseUri)
                .attributes(clientRegistrationId(clientRegistrationId))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
    }
}
