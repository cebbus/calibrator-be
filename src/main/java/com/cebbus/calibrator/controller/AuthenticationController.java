package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.security.AuthenticationRequest;
import com.cebbus.calibrator.security.AuthenticationResponse;
import com.cebbus.calibrator.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    @PostMapping
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        final String username = request.getUsername();
        final String password = request.getPassword();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = this.authenticationManager.authenticate(token);
        String authorizedToken = this.authenticationService.createToken(authenticate);
        return new AuthenticationResponse(authorizedToken);
    }

}
