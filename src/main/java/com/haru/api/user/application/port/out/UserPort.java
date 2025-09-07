package com.haru.api.user.application.port.out;

import com.haru.api.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserPort {

    Optional<User> findUserById(Long id);

    /**
     * 특정 문자열과 비슷한 이메일을 가진 사용자를 검색한다.
     */
    List<User> searchSimilarEmailUsers(String emailKeyword);

    /**
     * 이메일로 사용자를 찾는다.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * 소셜 로그인 제공자의 ID로 사용자를 찾는다.
     */
    Optional<User> findUserByProviderId(String providerId);

    /**
     * 사용자를 저장한다.
     */
    User saveUser(User user);

    /**
     * 이메일이 존재하는지 확인한다.
     */
    boolean existsUserByEmail(String email);

}
