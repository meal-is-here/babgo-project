package com.babgo.domain.review;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByStore_StoreId(UUID storeId, Sort sort);

    Optional<Review> findByOrderId(UUID orderId);

    boolean existsByStore_StoreId(UUID storeId);
}