package com.babgo.domain.review;

import com.babgo.domain.common.ActionType;
import java.util.UUID;

public record ReviewChangedEvent(UUID storeId, int newRating, int oldRating, ActionType action) {

}
