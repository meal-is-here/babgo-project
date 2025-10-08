package com.babgo.repository.user.impl;

import com.babgo.domain.user.User;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByUserIdAndDeletedAtIsNull(Long userId) {
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }
}