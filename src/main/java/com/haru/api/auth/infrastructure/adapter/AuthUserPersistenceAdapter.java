package com.haru.api.auth.infrastructure.adapter;

import com.haru.api.auth.application.port.out.AuthUserPort;
import com.haru.api.auth.infrastructure.jpa.AuthUserJpaRepository;
import com.haru.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserPersistenceAdapter implements AuthUserPort {

    private final AuthUserJpaRepository authUserJpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return authUserJpaRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return authUserJpaRepository.save(user);
    }
}
