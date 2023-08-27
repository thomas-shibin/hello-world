package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestWebAPI {

    @GetMapping
    public String sayHello(){
        return "Hi From Spring Boot";
    }
}
