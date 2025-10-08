package com.babgo.repository.user.impl;

import com.babgo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);
}