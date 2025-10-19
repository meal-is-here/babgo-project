package com.babgo.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByUserUserIdAndMenuMenuId(Long userId, UUID menuId);
    Optional<Favorite> findByFavoriteIdAndUserUserId(UUID favoriteId, Long userId);
    List<Favorite> findAllByUserUserIdOrderByCreatedAtDesc(Long userId);
}