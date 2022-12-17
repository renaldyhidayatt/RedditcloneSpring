package com.sanedge.reditclone.services;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.reditclone.dto.AuthenticationResponse;
import com.sanedge.reditclone.dto.LoginRequest;
import com.sanedge.reditclone.dto.MessageResponse;
import com.sanedge.reditclone.dto.SignupRequest;
import com.sanedge.reditclone.dto.UserResponse;
import com.sanedge.reditclone.exception.SpringRedditException;
import com.sanedge.reditclone.models.NotificationEmail;
import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.models.VerificationToken;
import com.sanedge.reditclone.repository.UserRepository;
import com.sanedge.reditclone.repository.VerificationTokenRepository;
import com.sanedge.reditclone.security.JwtUtils;

import lombok.extern.log4j.Log4j2;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder encoder, JwtUtils jwtUtils, VerificationTokenRepository verificationTokenRepository,
            MailService mailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateAccessToken(authentication);

        long expiresAt = jwtUtils.getjwtExpirationMs();
        Date date = new Date();
        date.setTime(expiresAt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return new AuthenticationResponse(jwt, date.toString(), userDetails.getUsername());
    }

    public MessageResponse register(SignupRequest signupRequest) {
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encoder.encode(signupRequest.getPassword()));
        user.setEnabled(false);

        this.userRepository.save(user);
        String tokenVerification = generateVerificationToken(user);

        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                        "please click on the below url to activate your account : " +
                        "http://localhost:8080/api/auth/accountVerification/" + tokenVerification));

        return new MessageResponse("Success create user");

    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserByPrincipal(UserDetails user) {
        User userRepo = this.userRepository.findByUsername(user.getUsername())
                .orElseThrow(
                        () -> new UsernameNotFoundException("User name not found - " + user.getUsername()));

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(userRepo.getEmail());
        userResponse.setUsername(userRepo.getUsername());
        userResponse.setCreated(userRepo.getCreated());

        return userResponse;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(
                        () -> new UsernameNotFoundException("User name not found - " + authentication.getName()));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void verififyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token")));
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}
