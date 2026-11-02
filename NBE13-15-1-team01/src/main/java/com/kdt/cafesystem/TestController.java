package com.kdt.cafesystem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/github-test")
    public String githubTest(@RequestParam(value = "name", defaultValue = "Developer") String name) {
        return "Hello, " + name + "! GitHub push and commit test is successful! 🚀";
    }
}