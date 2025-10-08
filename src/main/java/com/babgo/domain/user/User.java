package com.babgo.domain.user;

import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티
 * p_users 테이블과 매핑
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
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
        return new User(
                null,
                email,
                encodedPassword,
                name,
                nickname,
                phoneNumber,
                UserRole.CUSTOMER,
                false,
                null,
                true,
                null
        );
    }

    /**
     * 사장 사용자 생성 팩토리 메서드
     */
    public static User ofOwner(String email, String encodedPassword,
                               String name, String nickname, String phoneNumber) {
        return new User(
                null,
                email,
                encodedPassword,
                name,
                nickname,
                phoneNumber,
                UserRole.OWNER,
                false,
                null,
                true,
                null
        );
    }

    // TODO: 소프트 딜리트 처리 메소드를 작성해야 합니다
    // - isUserDeleted를 true로 설정
    // - deletedAt 시간 설정 (BaseTimeEntity의 markAsDeleted 호출)
    // - deletedBy에 삭제한 사용자 ID 설정
    public void delete(String deletedBy) {
        // 구현 필요
    }

    // TODO: 복구 메소드를 작성해야 합니다
    // - isUserDeleted를 false로 설정
    // - deletedAt을 null로 설정 (BaseTimeEntity의 restore 호출)
    // - deletedBy를 null로 설정
    public void restore() {
        // 구현 필요
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

    // TODO: 비밀번호 변경 메소드를 작성해야 합니다
    // - BCryptPasswordEncoder로 암호화된 비밀번호를 받아서 설정
    public void updatePassword(String encodedPassword) {
        // 구현 필요
    }

    // TODO: 권한 변경 메소드를 작성해야 합니다
    // - 관리자가 사용자의 권한을 변경할 때 사용
    public void updateRole(UserRole newRole) {
        // 구현 필요
    }
}
