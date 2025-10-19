package com.babgo.domain.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserUserIdAndStoreStoreId(Long userId, UUID storeId);
    boolean existsByUserUserIdAndStoreStoreId(Long userId, UUID storeId);
}
