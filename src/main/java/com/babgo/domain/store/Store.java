package com.babgo.domain.store;

import com.babgo.domain.ai.store_summary.StoreSummary;
import com.babgo.domain.store.status.StoreStatus;
import com.babgo.global.entity.BaseTimeEntity;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

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
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // by세준, db테이블에 영향x, 객체 메서드를 사용하기 위한 추가 컬럼입니다.
    @Getter
    @Setter
    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StoreSummary storeSummary;

    private Store(
            String storeName,
            String addressLine,
            double latitude,
            double longitude,
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
            double latitude,
            double longitude,
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

    public void markOwnerName(String ownerName) {
        if (createdBy == null || createdBy.isBlank()) {
            this.createdBy = "가게 사장님 이름";
        }
    }

    private static void validateLength(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 100) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }
    }

    private static void validateLatAndLon(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }

        if (longitude < -180.0 || longitude > 180.0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }

    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }

        if (!phoneNumber.matches("^[0-9-]+$")) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }

        // 2) 하이픈 사용 여부에 따라 정확한 형식 검증
        if (phoneNumber.contains("-")) {
            // 예: 02-123-4567 / 031-1234-5678 / 010-1234-5678
            if (!phoneNumber.matches("^0\\d{1,2}-\\d{3,4}-\\d{4}$")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
            }
        } else {
            // 하이픈 미사용: 총 9~11자리(0으로 시작)
            if (!phoneNumber.matches("^0\\d{8,10}$")) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
            }
        }

        // 3) 보조 안전망: 하이픈 제거 후 자리수 재확인(9~11)
        String digits = phoneNumber.replaceAll("-", "");
        if (digits.length() < 9 || digits.length() > 11) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }
    }

    private static void validateMinOrderAmount(int minOrderAmount) {
        if (minOrderAmount < 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }
    }

    private static void validateBusinessHours(LocalTime openingHours, LocalTime closingHours) {
        if (openingHours == null || closingHours == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }

        if (openingHours.equals(closingHours)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "에러 메시지 수정해주세요");
        }
    }

}
