package com.babgo.domain.user;

import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티
 * p_users 테이블과 매핑
 */
@Entity
@Table(name = "p_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

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
    @Builder.Default
    private Boolean isUserDeleted = false;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "is_profile_public")
    @Builder.Default
    private Boolean isProfilePublic = true;

    @Column(name = "profile_public_at")
    private java.time.LocalDateTime profilePublicAt;

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

    // TODO: 프로필 공개 설정 변경 메소드를 작성해야 합니다
    // - isProfilePublic 값을 변경
    // - 공개로 변경시 profilePublicAt에 현재 시간 설정
    public void updateProfilePublic(boolean isPublic) {
        // 구현 필요
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

    // TODO: 사용자 정보 수정 메소드를 작성해야 합니다
    // - nickname, phoneNumber 등 변경 가능한 정보 업데이트
    public void updateUserInfo(String nickname, String phoneNumber) {
        // 구현 필요
    }
}
