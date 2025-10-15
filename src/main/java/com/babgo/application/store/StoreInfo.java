package com.babgo.application.store;

import com.babgo.domain.store.Store;
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
        private final String regionCode;
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
                String regionCode,
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
                    regionCode,
                    phoneNumber,
                    minOrderAmount,
                    openingHours,
                    closingHours,
                    categoryId
            );
        }
    }

<<<<<<< Updated upstream
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Update {
        private final String storeName;
        private final String addressLine;
        private final Double latitude;
        private final Double longitude;
        private final String regionCode;
        private final String phoneNumber;
        private final Integer minOrderAmount;
        private final LocalTime openingHours;
        private final LocalTime closingHours;
        private final UUID categoryId;

        public static Update of(
                String storeName,
                String addressLine,
                Double latitude,
                Double longitude,
                String regionCode,
                String phoneNumber,
                Integer minOrderAmount,
                LocalTime openingHours,
                LocalTime closingHours,
                UUID categoryId) {
            return new Update(
                    storeName,
                    addressLine,
                    latitude,
                    longitude,
                    regionCode,
                    phoneNumber,
                    minOrderAmount,
                    openingHours,
                    closingHours,
                    categoryId
            );
        }
    }

=======
>>>>>>> Stashed changes
    // 단건 조회용 DTO
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {
        private final String storeName;
        private final String addressLine;
        private final String phoneNumber;
        private final int minOrderAmount;
        private final LocalTime openingHours;
        private final LocalTime closingHours;
        private final UUID categoryId;

        public static Detail fromEntity(Store store) {
            return new Detail(
                    store.getStoreName(),
                    store.getAddressLine(),
                    store.getPhoneNumber(),
                    store.getMinOrderAmount(),
                    store.getOpeningHours(),
                    store.getClosingHours(),
                    store.getCategory().getCategoryId()
            );
        }
    }

    // 요약 조회용 DTO (JSON 반환)
    @Getter
    @RequiredArgsConstructor
    public static class Summary {
        private final String summary;

        public static Summary of(String summary) {
            return new Summary(summary);
        }
    }
}
