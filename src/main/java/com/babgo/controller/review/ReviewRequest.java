package com.babgo.controller.review;


import java.util.UUID;

public class ReviewRequest {
    private UUID storeId;
    private Long userId;
    private String content;
    private int rating;

    public UUID getStoreId() { return storeId; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
}
