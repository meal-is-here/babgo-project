package com.babgo.application.store;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreInfo {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final String storeName;
        private final String addressLine;
        private final double latitude;
        private final double longitude;
        private final String phoneNumber;
        private final int minOrderAmount;
        private final LocalTime openingHours;
        private final LocalTime closingHours;
        private final UUID categoryId;

        public static Create of(
                String storeName,
                String addressLine,
                double latitude,
                double longitude,
                String phoneNumber,
                int minOrderAmount,
                LocalTime openingHours,
                LocalTime closingHours,
                UUID categoryId) {
            return new Create(
                    storeName,
                    addressLine,
                    latitude,
                    longitude,
                    phoneNumber,
                    minOrderAmount,
                    openingHours,
                    closingHours,
                    categoryId
            );
        }
    }
}
