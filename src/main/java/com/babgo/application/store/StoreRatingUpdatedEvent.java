package com.babgo.application.store;

import java.util.UUID;

public record StoreRatingUpdatedEvent(UUID storeId, UUID categoryId, String regionCode, double averageRatinge) {

}
