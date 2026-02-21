package com.rohithv07.bookkeeping.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @GetMapping
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
