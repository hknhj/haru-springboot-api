package com.haru.api.user.infrastructure.adapter;

import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.infrastructure.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public List<User> searchSimilarEmailUsers(String emailKeyword) {
        return userJpaRepository.findTop4UsersByEmailContainingIgnoreCase(emailKeyword);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByProviderId(String providerId) {
        return userJpaRepository.findByProviderId(providerId);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
