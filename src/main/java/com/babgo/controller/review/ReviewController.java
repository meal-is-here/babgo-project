package com.babgo.controller.review;

import com.babgo.application.review.ReviewFacade;
import com.babgo.application.review.ReviewInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewFacade reviewFacade;

    @PostMapping
    public ResponseEntity<ReviewResponse> postReview(@RequestBody ReviewRequest req) {
        ReviewInfo info = reviewFacade.createReview(req.getStoreId(), req.getUserId(), req.getContent(), req.getRating());
        ReviewResponse res = new ReviewResponse(
                info.getReviewId(),
                info.getStoreId(),
                info.getUserId(),
                info.getContent(),
                info.getRating(),
                info.getCreatedAt()
        );
        return ResponseEntity.ok(res);
    }
}
