package com.haru.api.infra.security.login;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomDetailsService {
    UserDetails loadUserByUsername(String username, String password) throws UsernameNotFoundException;
}
