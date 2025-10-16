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

    @Column(nullable = false)
    private String regionCode;

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
            String regionCode,
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
        validateRegionCode(regionCode);
        validatePhoneNumber(phoneNumber);
        validateMinOrderAmount(minOrderAmount);
        validateBusinessHours(openingHours, closingHours);
        validateCategory(category);

        this.storeName = storeName;
        this.addressLine = addressLine;
        this.latitude = latitude;
        this.longitude = longitude;
        this.regionCode = regionCode;
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
            String regionCode,
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
                regionCode,
                phoneNumber,
                minOrderAmount,
                openingHours,
                closingHours,
                StoreStatus.PREPARING,
                category
        );
    }

    public void markCreateBy(String ownerName) {
        if (createdBy == null || createdBy.isBlank()) {
            this.createdBy = "ownerName";
        }
    }

    public void markUpdatedBy(String ownerName) {
        if (updatedBy == null || updatedBy.isBlank()) {
            this.updatedBy = "ownerName";
        }
    }

    public void markDeletedBy(String ownerName) {
        if (isDeleted()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 삭제된 가게입니다.");
        }
        markAsDeleted();
        this.deletedBy = "ownerName";
    }

    public boolean isDeleted() {
        return getDeletedAt() != null;
    }

    public void changeStoreName(String storeName) {
        validateLength(storeName);
        this.storeName = storeName;
    }

    public void changeAddressLine(String addressLine) {
        validateLength(addressLine);
        this.addressLine = addressLine;
    }

    public void changeLocation(double lat, double lon) {
        validateLatAndLon(lat, lon);
        this.latitude = lat;
        this.longitude = lon;
    }

    public void changeRegionCode(String regionCode) {
        validateRegionCode(regionCode);
        this.regionCode = regionCode;
    }

    public void changePhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    public void changeMinOrderAmount(Integer minOrderAmount) {
        validateMinOrderAmount(minOrderAmount);
        this.minOrderAmount = minOrderAmount;
    }

    public void changeBusinessHours(LocalTime open, LocalTime close) {
        validateBusinessHours(open, close);
        this.openingHours = open;
        this.closingHours = close;
    }

    public void changeCategory(Category category) {
        validateCategory(category);
        this.category = category;
    }

    private static void validateLength(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 100) {
            throw new CustomException(ErrorCode.INVALID, "1~100자여야 합니다.");
        }
    }

    private static void validateLatAndLon(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new CustomException(ErrorCode.INVALID, "위도는 -90 이상 90 이하여야 합니다.");
        }

        if (longitude < -180.0 || longitude > 180.0) {
            throw new CustomException(ErrorCode.INVALID, "경도는 -180 이상 180 이하여야 합니다.");
        }
    }

    private static void validateRegionCode(String regionCode) {
        if (regionCode == null || regionCode.isBlank()) {
            throw new CustomException(ErrorCode.INVALID, "행정코드값은 필수입니다.");
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new CustomException(ErrorCode.INVALID, "전화번호는 필수입니다.");
        }

        if (!phoneNumber.matches("^[0-9-]+$")) {
            throw new CustomException(ErrorCode.INVALID, "전화번호는 숫자와 하이픈(-)만 사용할 수 있습니다.");
        }

        // 2) 하이픈 사용 여부에 따라 정확한 형식 검증
        if (phoneNumber.contains("-")) {
            // 예: 02-123-4567 / 031-1234-5678 / 010-1234-5678
            if (!phoneNumber.matches("^0\\d{1,2}-\\d{3,4}-\\d{4}$")) {
                throw new CustomException(ErrorCode.INVALID, "전화번호 형식이 올바르지 않습니다. 예) 02-123-4567, 031-1234-5678, 010-1234-5678");
            }
        } else {
            // 하이픈 미사용: 총 9~11자리(0으로 시작)
            if (!phoneNumber.matches("^0\\d{8,10}$")) {
                throw new CustomException(ErrorCode.INVALID, "하이픈 없이 입력 시 0으로 시작하는 9~11자리여야 합니다. 예) 01012345678");
            }
        }

        // 3) 보조 안전망: 하이픈 제거 후 자리수 재확인(9~11)
        String digits = phoneNumber.replaceAll("-", "");
        if (digits.length() < 9 || digits.length() > 11) {
            throw new CustomException(ErrorCode.INVALID, "전화번호 숫자 길이는 9~11자리여야 합니다.");
        }
    }

    private static void validateMinOrderAmount(int minOrderAmount) {
        if (minOrderAmount < 0) {
            throw new CustomException(ErrorCode.INVALID, "최소 주문 금액은 0 이상이어야 합니다.");
        }
    }

    private static void validateBusinessHours(LocalTime openingHours, LocalTime closingHours) {
        if (openingHours == null || closingHours == null) {
            throw new CustomException(ErrorCode.INVALID, "영업 시작/종료 시간은 필수입니다.");
        }

        if (openingHours.equals(closingHours)) {
            throw new CustomException(ErrorCode.INVALID, "영업 시작 시간과 종료 시간이 같을 수 없습니다.");
        }
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new CustomException(ErrorCode.INVALID, "카테고리는 필수입니다.");
        }
    }

    /**
     * 주문 생성 시 가게의 운영 상태를 확인하기 위해 사용합니다.
     * 가게의 오픈 시간과 클로스 시간 사이에 해당하면 true
     * 확인 후 주석 삭제해주세요
     */
    public boolean isOrderable(LocalTime now) {
        if (this.getDeletedAt() != null) return false;
        if (this.storeStatus != StoreStatus.OPEN) return false;

        LocalTime open = openingHours;
        LocalTime close = closingHours;
        boolean crossMidnight = close.isBefore(open);

        if (!crossMidnight) {
            return !now.isBefore(open) && now.isBefore(close);
        } else {
            return !now.isBefore(open) || now.isBefore(close);
        }
    }
}
