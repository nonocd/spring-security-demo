package com.stvd.oauth2.client.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Controller
public class DemoController {
    @Autowired
    private WebClient webClient;

    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(value = "name", defaultValue = "world") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/remote/hello")
    @ResponseBody
    public String remoteHello(@RequestParam(value = "name", defaultValue = "world") String name) {
        String msg = webClient.get()
                .uri("/hello")
                .attributes(clientRegistrationId("client-password"))
                .retrieve()
                .toEntity(String.class)
                .block()
                .getBody();
        return msg;
    }
}