package com.linkup.app.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("user/")
public class UserController {

    @GetMapping("/")
    String getHello() {
        return "Hello";
    }

    
}
