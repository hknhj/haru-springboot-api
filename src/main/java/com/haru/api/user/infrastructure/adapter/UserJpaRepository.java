package com.haru.api.user.infrastructure.adapter;

import com.haru.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    List<User> findTop4UsersByEmailContainingIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);

    boolean existsByEmail(String email);

}
