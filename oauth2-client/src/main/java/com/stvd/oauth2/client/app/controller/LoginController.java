package com.stvd.oauth2.client.app.controller;

import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LoginController {

    @GetMapping("/login/oauth2/code/{clientId}")
    public Object getAuthCode(@PathVariable String clientId, String code, String state) {

        return String.format("clientId: %s, code: %s, state: %s", clientId, code, state);
    }
}
