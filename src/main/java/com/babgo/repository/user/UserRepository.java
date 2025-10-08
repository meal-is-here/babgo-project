package com.babgo.repository.user;

import com.babgo.domain.user.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    void deleteAll();
}
