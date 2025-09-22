package com.haru.api.auth.application.port.out;

import org.springframework.security.core.Authentication;

public interface AuthenticatePort {

    Authentication authenticate(String email, String password);

}
