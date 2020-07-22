package com.stvd.oauth2.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "world") String name) {

        var log = switch (name) {
            case "hello" -> "hello " + name;
            default -> "";
        };
        return String.format("Hello %s!", name);
    }
}
