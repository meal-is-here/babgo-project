package com.babgo.domain.store;

import com.babgo.domain.store.status.StoreStatus;
import com.babgo.global.entity.BaseTimeEntity;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Column(nullable = false, length = 100)
    private String addressLine;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private int minOrderAmount;

    @Column(nullable = false)
    private LocalTime openingHours;

    @Column(nullable = false)
    private LocalTime closingHours;

    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus = StoreStatus.PREPARING;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private String updatedBy;

    private String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Store(
            String storeName,
            String addressLine,
            BigDecimal latitude,
            BigDecimal longitude,
            String phoneNumber,
            int minOrderAmount,
            LocalTime openingHours,
            LocalTime closingHours,
            StoreStatus storeStatus,
            Category category
    ) {
        validateLength(storeName);
        validateLength(addressLine);
        validateLatAndLon(latitude, longitude);
        validatePhoneNumber(phoneNumber);
        validateMinOrderAmount(minOrderAmount);
        validateBusinessHours(openingHours, closingHours);
        validateCategory(category);

        this.storeName = storeName;
        this.addressLine = addressLine;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.minOrderAmount = minOrderAmount;
        this.openingHours = openingHours;
        this.closingHours = closingHours;
        this.storeStatus = storeStatus;
        this.category = category;
    }

    /**
     * StoreStatus 는 생성 시점에 기본 값: PREPARING
     */
    public static Store of(
            String storeName,
            String addressLine,
            BigDecimal latitude,
            BigDecimal longitude,
            String phoneNumber,
            int minOrderAmount,
            LocalTime openingHours,
            LocalTime closingHours,
            Category category
    ) {
        return new Store(
                storeName,
                addressLine,
                latitude,
                longitude,
                phoneNumber,
                minOrderAmount,
                openingHours,
                closingHours,
                StoreStatus.PREPARING,
                category
        );
    }

    private static void validateLength(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 100) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static void validateLatAndLon(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (phoneNumber.length() > 20) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        String digits = normalizePhone(phoneNumber);
        if (!digits.matches("^0\\d{8,10}$")) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static String normalizePhone(String phoneNumber) {
        return phoneNumber.replaceAll("\\D", "");
    }

    private static void validateMinOrderAmount(int minOrderAmount) {
        if (minOrderAmount < 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static void validateBusinessHours(LocalTime openingHours, LocalTime closingHours) {
        if (openingHours == null || closingHours == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (openingHours.equals(closingHours)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }

}
