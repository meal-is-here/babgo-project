package com.babgo.repository.user;

import com.babgo.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // TODO: 필요시 추가 메소드를 작성하세요
    // - Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    // - List<UserEntity> findByRole(UserRole role);
    // - List<UserEntity> findByIsUserDeletedFalse();  // 삭제되지 않은 사용자만 조회
}
