package com.sanedge.reditclone.controllers;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.payload.request.LoginRequest;
import com.sanedge.reditclone.payload.request.SignupRequest;
import com.sanedge.reditclone.payload.response.AuthenticationResponse;
import com.sanedge.reditclone.payload.response.MessageResponse;
import com.sanedge.reditclone.repository.UserRepository;
import com.sanedge.reditclone.security.JwtUtils;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateAccessToken(authentication);

        long expiresAt = jwtUtils.getjwtExpirationMs();
        Date date = new Date();
        date.setTime(expiresAt);

        User userDetails = (User) authentication.getPrincipal();

        return ResponseEntity
                .ok(new AuthenticationResponse(jwt, date.toString(), userDetails.getUsername()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody SignupRequest signupRequest) {
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encoder.encode(signupRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Success create user"));

    }

}
