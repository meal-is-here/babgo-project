package com.babgo.repository.user;

import com.babgo.domain.user.UserEntity;

import java.util.Optional;

public interface UserRepository {

    UserEntity save(UserEntity user);

    Optional<UserEntity> findById(String userId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
