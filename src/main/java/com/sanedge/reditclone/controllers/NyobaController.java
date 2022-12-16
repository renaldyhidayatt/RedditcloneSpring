package com.sanedge.reditclone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanedge.reditclone.dto.UserResponse;
import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.services.AuthService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/nyoba")
public class NyobaController {

    private final AuthService authService;

    @Autowired
    public NyobaController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/nama")
    public ResponseEntity<User> getNama() {
        User userResponse = this.authService.getCurrentUser();
        return ResponseEntity.ok(userResponse);
    }
}
