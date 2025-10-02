package com.babgo.domain.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RefreshToken Redis Repository
 *
 * Redis에 저장된 Refresh Token을 관리합니다.
 * CrudRepository를 상속받아 기본 CRUD 기능을 제공합니다.
 *
 * TODO: 이 Repository가 제공하는 기본 메소드들
 * - save(RefreshToken) : Redis에 RefreshToken 저장
 * - findById(String userId) : userId로 RefreshToken 조회
 * - deleteById(String userId) : userId로 RefreshToken 삭제 (로그아웃시 사용)
 * - existsById(String userId) : userId의 RefreshToken 존재 여부 확인
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    /**
     * TODO: userId로 RefreshToken을 조회하는 메소드
     * - CrudRepository의 findById와 동일하지만 명확한 이름으로 재정의
     * - Optional로 반환하여 null 안전성 보장
     *
     * @param userId 사용자 ID
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByUserId(String userId);

    /**
     * TODO: token 값으로 RefreshToken을 조회하는 메소드
     * - Refresh Token 재발급시 기존 토큰으로 검증할 때 사용
     * - Optional로 반환하여 null 안전성 보장
     *
     * @param token Refresh Token 값
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * TODO: userId로 RefreshToken을 삭제하는 메소드
     * - 로그아웃시 사용
     * - CrudRepository의 deleteById와 동일하지만 명확한 이름으로 재정의
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);
}