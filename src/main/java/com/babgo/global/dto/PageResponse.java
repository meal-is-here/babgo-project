package com.babgo.global.dto;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class PageResponse<T> {

	private final List<T> content;

	private final long totalElements;
	private final int totalPages;
	private final int page;
	private final int size;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final String sort; // Optional: 정렬 정보

	// Spring Data Page<T> 변환용
	public static <T> PageResponse<T> fromPage(Page<T> pageData) {
		return PageResponse.<T>builder()
						   .content(pageData.getContent() == null ? Collections.emptyList() : pageData.getContent())
						   .totalElements(pageData.getTotalElements())
						   .totalPages(pageData.getTotalPages())
						   .page(pageData.getNumber())
						   .size(pageData.getSize())
						   .sort(pageData.getSort().toString())
						   .build();
	}

	// 커스텀 리스트 변환용
	public static <T> PageResponse<T> of(List<T> content, long totalElements, int totalPages, int page, int size) {
		return PageResponse.<T>builder()
						   .content(content == null ? Collections.emptyList() : content)
						   .totalElements(totalElements)
						   .totalPages(totalPages)
						   .page(page)
						   .size(size)
						   .build();
	}
}