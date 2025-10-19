package com.babgo.domain.like;

import com.babgo.domain.common.ActionType;
import java.util.UUID;

public record LikeChangedEvent(UUID storeId, ActionType action) {

}
