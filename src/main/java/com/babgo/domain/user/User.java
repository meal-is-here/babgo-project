package com.babgo.domain.user;

import com.babgo.global.entity.BaseTimeEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 사용자 엔티티
 * p_users 테이블과 매핑
 */
@Entity
@Getter
@Table(name = "p_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID publicId;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address_line", length = 100)
    private String addressLine = "서울특별시 종로구 세종대로 172";  // 기본값: 광화문

    @Column(name = "latitude")
    private double latitude = 37.5759;  // 광화문 위도

    @Column(name = "longitude")
    private double longitude = 126.9768;  // 광화문 경도

    @Column(name = "administrative_code", length = 10)
    private String administrativeCode = "1111054000";  // 행정코드: 서울특별시 종로구 사직동

    @Column(name = "legal_code", length = 10)
    private String legalCode = "1111010300";  // 법정코드: 서울특별시 종로구 사직동

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "is_user_deleted")
    private Boolean isUserDeleted = false;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "is_profile_public")
    private Boolean isProfilePublic = true;

    @Column(name = "profile_public_at")
    private java.time.LocalDateTime profilePublicAt;

    /**
     * 고객 사용자 생성 팩토리 메서드
     */
    public static User ofCustomer(String email, String encodedPassword,
                                  String name, String nickname, String phoneNumber) {
        User user = new User();
        user.publicId = UuidCreator.getTimeOrderedEpoch();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.role = UserRole.CUSTOMER;
        user.isUserDeleted = false;
        user.isProfilePublic = true;
        return user;
    }

    /**
     * 사장 사용자 생성 팩토리 메서드
     */
    public static User ofOwner(String email, String encodedPassword,
                               String name, String nickname, String phoneNumber) {
        User user = new User();
        user.publicId = UuidCreator.getTimeOrderedEpoch();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.role = UserRole.OWNER;
        user.isUserDeleted = false;
        user.isProfilePublic = true;
        return user;
    }

    /**
     * 사용자 소프트 삭제
     */
    @Override
    public void markAsDeleted() {
        this.isUserDeleted = true;
        super.markAsDeleted();
    }

    /**
     * 소프트 딜리트된 사용자 복구
     */
    public void restore() {
        this.isUserDeleted = false;
        this.deletedBy = null;
        super.restore();
    }

    public void updateProfile(String email, String encodedPassword, String name,
                              String nickname, String phoneNumber, Boolean isProfilePublic) {
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (encodedPassword != null && !encodedPassword.isBlank()) {
            this.password = encodedPassword;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (isProfilePublic != null) {
            this.isProfilePublic = isProfilePublic;
            if (isProfilePublic) {
                this.profilePublicAt = java.time.LocalDateTime.now();
            }
        }
    }
}