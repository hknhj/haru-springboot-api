package com.haru.api.infra.security.login;

import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements CustomDetailsService {

    private final UserPort userPort;

    @Override
    public UserDetails loadUserByUsername(String email, String password) throws UsernameNotFoundException {
        User user = userPort.findUserByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_USERNAME_NOT_FOUND));

        org.springframework.security.core.userdetails.User securityUser = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );

        return securityUser;
    }
}
