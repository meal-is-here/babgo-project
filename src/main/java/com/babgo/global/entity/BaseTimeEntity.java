package com.babgo.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass // 테이블 생성 X, 상속 시 컬럼으로 포함
@EntityListeners(AuditingEntityListener.class) // JPA 이벤트 리스너 등록(수정시간, 생성시간 자동화)
public abstract class BaseTimeEntity {

	@CreatedDate
	@Column(updatable = false)
	protected LocalDateTime createdAt;

	@LastModifiedDate
	protected LocalDateTime updatedAt;

	protected LocalDateTime deletedAt;
}