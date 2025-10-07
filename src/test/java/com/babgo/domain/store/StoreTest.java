package com.babgo.domain.store;

import com.babgo.domain.store.status.StoreStatus;
import com.babgo.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreTest {

    private static final String VALID_NAME = "홍대 김치찌개";
    private static final String VALID_ADDR = "서울시 마포구 양화로 123";
    private static final double VALID_LAT = 37.5665;
    private static final double VALID_LON = 126.9780;
    private static final String VALID_PHONE = "02-1234-5678";
    private static final int VALID_MIN_ORDER = 15000;
    private static final LocalTime VALID_OPEN = LocalTime.of(9, 0);
    private static final LocalTime VALID_CLOSE = LocalTime.of(21, 0);

    @DisplayName("Store 객체가 정상적으로 생성된다.(StoreStatus는 PREPARING)")
    @Test
    void create_success() {
        // given
        UUID catId = UUID.randomUUID();
        Category category = categoryWith(catId);
        // when
        Store store = Store.of(
                VALID_NAME,
                VALID_ADDR,
                VALID_LAT,
                VALID_LON,
                VALID_PHONE,
                VALID_MIN_ORDER,
                VALID_OPEN,
                VALID_CLOSE,
                category
        );
        assertThat(store)
                .extracting(
                        Store::getStoreName,
                        Store::getAddressLine,
                        Store::getLatitude,
                        Store::getLongitude,
                        Store::getPhoneNumber,
                        Store::getMinOrderAmount,
                        Store::getOpeningHours,
                        Store::getClosingHours,
                        Store::getStoreStatus
                )
                .containsExactly(
                        VALID_NAME,
                        VALID_ADDR,
                        VALID_LAT,
                        VALID_LON,
                        VALID_PHONE,
                        VALID_MIN_ORDER,
                        VALID_OPEN,
                        VALID_CLOSE,
                        StoreStatus.PREPARING
                );
        assertThat(store.getCategory()).isNotNull();
        assertThat(store.getCategory().getCategoryId()).isEqualTo(catId);
        assertThat(store.getStoreStatus()).isEqualTo(StoreStatus.PREPARING);
    }

    @DisplayName("changeStoreName: 유효한 이름으로 변경된다")
    @Test
    void changeStoreName_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changeStoreName("새로운 가게명");
        assertThat(store.getStoreName()).isEqualTo("새로운 가게명");
    }

    @DisplayName("changeAddressLine: 유효한 주소로 변경된다.")
    @Test
    void changeAddressLine_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changeAddressLine("서울시 어딘가 123");
        assertThat(store.getAddressLine()).isEqualTo("서울시 어딘가 123");
    }

    @DisplayName("changeLocation: 유효한 위경도로 변경된다.")
    @Test
    void changeLocation_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changeLocation(12.34, 56.78);
        assertThat(store.getLatitude()).isEqualTo(12.34);
        assertThat(store.getLongitude()).isEqualTo(56.78);
    }

    @DisplayName("changePhoneNumber: 유효한 번호로 변경된다")
    @Test
    void changePhoneNumber_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changePhoneNumber("010-1234-5678");
        assertThat(store.getPhoneNumber()).isEqualTo("010-1234-5678");

        store.changePhoneNumber("0212345678");
        assertThat(store.getPhoneNumber()).isEqualTo("0212345678");
    }

    @DisplayName("changeMinOrderAmount: 유효한 최소주문금액으로 변경한다.")
    @Test
    void changeMinOrderAmount_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changeMinOrderAmount(0);
        assertThat(store.getMinOrderAmount()).isEqualTo(0);

        store.changeMinOrderAmount(9999);
        assertThat(store.getMinOrderAmount()).isEqualTo(9999);
    }

    @DisplayName("changeBusinessHours: 유효한 시간으로 변경된다.")
    @Test
    void changeBusinessHours_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));

        store.changeBusinessHours(LocalTime.of(10,0), LocalTime.of(20,0));
        assertThat(store.getOpeningHours()).isEqualTo(LocalTime.of(10,0));
        assertThat(store.getClosingHours()).isEqualTo(LocalTime.of(20,0));
    }

    @DisplayName("changeCategory: 새 카테고리로 변경된다")
    @Test
    void changeCategory_success() {
        Store store = Store.of(VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE, categoryWith(UUID.randomUUID()));
        Category newCategory = categoryWith(UUID.randomUUID());

        store.changeCategory(newCategory);
        assertThat(store.getCategory()).isSameAs(newCategory);
    }

    @ParameterizedTest
    @MethodSource("invalidLength")
    @DisplayName("가게 이름이 null/blank/101자 초과하여 객체 생성을 실패한다.")
    void name_invalid(String name) {
        assertThatThrownBy(() -> Store.of(
                name, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE,
                categoryWith(UUID.randomUUID())
        )).isInstanceOf(CustomException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidLength")
    @DisplayName("가게 상세주소가 null/blank/101자 초과하여 객체 생성을 실패한다.")
    void address_invalid(String address) {
        assertThatThrownBy(() -> Store.of(
                VALID_NAME, address, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE,
                categoryWith(UUID.randomUUID())
        )).isInstanceOf(CustomException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidLatLon")
    @DisplayName("위/경도(범위초과)면 객체 생성에 실패한다.")
    void lat_and_lon_invalid(double lat, double lon) {
        assertThatThrownBy(() -> Store.of(
                VALID_NAME, VALID_ADDR, lat, lon,
                VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE,
                categoryWith(UUID.randomUUID())
        )).isInstanceOf(CustomException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidPhones")
    @DisplayName("잘못된 전화번호 형식이면 객체 생성에 실패한다.")
    void phone_invalid(String phone) {
        assertThatThrownBy(() -> Store.of(
                VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                phone, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE,
                categoryWith(UUID.randomUUID())
        )).isInstanceOf(CustomException.class);
    }

    @DisplayName("최소 주문금액이 음수여서 객체 생성에 실패한다.")
    @Test
    void min_order_amount_negative() {
        assertThatThrownBy(
                () -> Store.of(
                        VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                        VALID_PHONE, -1, VALID_OPEN, VALID_CLOSE,
                        categoryWith(UUID.randomUUID())
                )).isInstanceOf(CustomException.class);
    }

    @DisplayName("영업&운영 시간이 null이면 객체 생성에 실패한다.")
    @ParameterizedTest
    @MethodSource("invalidBusinessHoursNullOnly")
    void business_hours_invalid(LocalTime openingHours, LocalTime closingHours) {
        assertThatThrownBy(() -> Store.of(
                VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                VALID_PHONE, VALID_MIN_ORDER, openingHours, closingHours,
                categoryWith(UUID.randomUUID())
        )).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("실패: 카테고리 null이면 예외")
    void category_null() {
        assertThatThrownBy(
                () -> Store.of(
                        VALID_NAME, VALID_ADDR, VALID_LAT, VALID_LON,
                        VALID_PHONE, VALID_MIN_ORDER, VALID_OPEN, VALID_CLOSE,
                        null
                )).isInstanceOf(CustomException.class);
    }

    private Category categoryWith(UUID id) {
        try {
            Category c = Category.class.getDeclaredConstructor().newInstance();
            Field idF = Category.class.getDeclaredField("categoryId");
            idF.setAccessible(true);
            idF.set(c, id);
            Field nameF = Category.class.getDeclaredField("categoryName");
            nameF.setAccessible(true);
            nameF.set(c, "치킨");
            return c;
        } catch (Exception e) {
            throw new IllegalStateException("테스트용 Category 생성 실패", e);
        }
    }

    private static Stream<String> invalidLength() {
        return Stream.of(
                null,
                "",
                " ",
                "   ",
                "a".repeat(101)
        );
    }

    static Stream<Arguments> invalidLatLon() {
        return Stream.of(
                Arguments.of(-90.0001, VALID_LON),
                Arguments.of(90.0001, VALID_LON),
                Arguments.of(VALID_LAT, -180.0001),
                Arguments.of(VALID_LAT, 180.0001)
        );
    }

    static Stream<String> invalidPhones() {
        return Stream.of(
                null, "", " ",
                "0".repeat(21),
                "123456789",
                "010-12-3456",
                "010-12345-6789",
                "0a0-1234-5678"
        );
    }

    static Stream<Arguments> invalidBusinessHoursNullOnly() {
        return Stream.of(
                Arguments.of(null, LocalTime.of(21, 0)),
                Arguments.of(LocalTime.of(9, 0), null)
        );
    }
}