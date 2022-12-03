package com.cebbus.calibrator.service;

import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    String createToken(Authentication authResult);

}
