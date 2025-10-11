package com.babgo.domain.user;

import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
     * 소프트 딜리트 처리
     * @param deletedBy 삭제한 사용자 식별자
     */
    public void delete(String deletedBy) {
        this.isUserDeleted = true;
        this.deletedBy = deletedBy;
        this.markAsDeleted();
    }

    /**
     * 소프트 딜리트된 사용자 복구
     */
    public void restore() {
        this.isUserDeleted = false;
        this.deletedBy = null;
        super.restore();
    }

    /**
     * 사용자 정보 수정
     */
    public void updateUserInfo(String nickname, String phoneNumber) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 프로필 공개 설정 변경
     */
    public void updateProfilePublic(Boolean isPublic) {
        this.isProfilePublic = isPublic;
        if (isPublic) {
            this.profilePublicAt = java.time.LocalDateTime.now();
        }
    }

    /**
     * 비밀번호 변경
     * @param encodedPassword BCryptPasswordEncoder로 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 권한 변경 (관리자용)
     * @param newRole 새로운 권한
     */
    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void updateProfile(String email, String encodedPassword, String name, String nickname,
                              String phoneNumber, Boolean isProfilePublic) {

        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (encodedPassword != null && !encodedPassword.isBlank()) {
            this.password = encodedPassword;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }

        if (isProfilePublic != null && !isProfilePublic.equals(this.isProfilePublic)) {
            this.isProfilePublic = isProfilePublic;
            this.profilePublicAt = isProfilePublic ? LocalDateTime.now() : null;
        }
    }
}
