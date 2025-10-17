package com.babgo.domain.review;

import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    // read review by store
    public List<ReviewResponse> getReviewsByStore(UUID storeId, String sort) {
        if (!reviewRepository.existsByStore_StoreId(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        Sort sortOption = switch (sort) {
            case "high" -> Sort.by(Sort.Direction.DESC, "rating");
            case "low" -> Sort.by(Sort.Direction.ASC, "rating");
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
        };

        return reviewRepository.findByStore_StoreId(storeId, sortOption)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
