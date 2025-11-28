package com.sribalajiads.media_app.controller;

import com.sribalajiads.media_app.dto.LoginRequest;
import com.sribalajiads.media_app.dto.LoginResponse;
import com.sribalajiads.media_app.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // Allowing requests from our frontend
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // This will automatically use our UserDetailsService and PasswordEncoder to validate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // If authentication is successful, generate a token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}