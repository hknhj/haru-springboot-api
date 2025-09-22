package com.haru.api.auth.application.port.out;

import com.haru.api.user.domain.User;

import java.util.Optional;

public interface AuthUserPort {

    Optional<User> findByEmail(String email);;

    User save(User user);

}
