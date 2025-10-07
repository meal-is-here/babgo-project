package com.babgo.controller.store;

import com.babgo.application.store.StoreInfo;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

import static com.babgo.controller.store.ValidateGroups.*;

@Getter
@NoArgsConstructor
public class StoreRequest {

    @Getter
    @NoArgsConstructor
    public static class Upsert {

        @NotBlank(groups = OnCreate.class, message = "가게 이름은 필수 값입니다.")
        @Pattern(regexp = "\\S.*", message = "가게 이름은 공백만으로는 수정할 수 없습니다.")
        @Size(max = 100, message = "가게 이름은 최대 100자입니다.")
        private String storeName;

        @NotBlank(groups = OnCreate.class, message = "가게의 상세 주소는 필수 값입니다.")
        @Pattern(regexp = "\\S.*", message = "상세 주소는 공백만으로는 수정할 수 없습니다.")
        @Size(max = 100, message = "상세 주소는 최대 100자입니다.")
        private String addressLine;

        @NotNull(groups = OnCreate.class, message = "위도 값은 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        private Double latitude;

        @NotNull(groups = OnCreate.class, message = "경도 값은 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        private Double longitude;

        @NotBlank(groups = OnCreate.class, message = "전화번호는 필수 값입니다.")
        @Size(max = 20, message = "전화번호는 최대 20자입니다.")
        @Pattern(
                regexp = "^0[0-9-]{8,19}$",
                message = "전화번호는 0으로 시작하고 숫자와 하이픈만 사용할 수 있습니다. 예) 02-1234-5678 / 01012345678"
        )
        private String phoneNumber;

        @NotNull(groups = OnCreate.class, message = "최소주문금액은 필수 값입니다.")
        @PositiveOrZero(message = "최소 주문 금액은 0 이상이어야 합니다.")
        private Integer minOrderAmount;

        @NotNull(groups = OnCreate.class, message = "오픈 시간은 필수 값입니다.")
        private LocalTime openingHours;

        @NotNull(groups = OnCreate.class, message = "마감 시간은 필수 값입니다.")
        private LocalTime closingHours;

        @NotNull(groups = OnCreate.class, message = "카테고리 id은 필수 값입니다.")
        private UUID categoryId;

        private Upsert(String storeName, String addressLine, double latitude, double longitude, String phoneNumber, int minOrderAmount, LocalTime openingHours, LocalTime closingHours, UUID categoryId) {
            this.storeName = storeName;
            this.addressLine = addressLine;
            this.latitude = latitude;
            this.longitude = longitude;
            this.phoneNumber = phoneNumber;
            this.minOrderAmount = minOrderAmount;
            this.openingHours = openingHours;
            this.closingHours = closingHours;
            this.categoryId = categoryId;
        }

        public static Upsert of(String storeName, String addressLine, double latitude, double longitude, String phoneNumber, int minOrderAmount, LocalTime openingHours, LocalTime closingHours, UUID categoryId) {
            return new Upsert(storeName,addressLine, latitude, longitude, phoneNumber, minOrderAmount, openingHours, closingHours, categoryId);
        }

        public StoreInfo.Create toCreateInfo() {
            return StoreInfo.Create.of(storeName, addressLine, latitude, longitude, phoneNumber, minOrderAmount, openingHours, closingHours, categoryId);
        }

        public StoreInfo.Update toUpdateInfo() {
            return StoreInfo.Update.of(storeName, addressLine, latitude, longitude, phoneNumber, minOrderAmount, openingHours, closingHours, categoryId);
        }
    }
}
