package com.babgo.repository.user;

import com.babgo.domain.user.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUserId(String userId);
    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);
}