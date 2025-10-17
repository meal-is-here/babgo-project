package com.babgo.application.store;

import java.util.UUID;

public record StoreOrderCompletedEvent(UUID storeId, UUID categoryId, String regionCode) {

}
