package com.haru.api.user.infrastructure;

import com.haru.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findTop4UsersByEmailContainingIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);

}
