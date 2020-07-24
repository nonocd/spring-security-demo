package com.stvd.oauth2.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

    @GetMapping("/hello")
    @ResponseBody
    public ResponseEntity<String> hello(@RequestParam(value = "name", defaultValue = "world") String name) {
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(String.format("Hello %s!", name));
    }
}
