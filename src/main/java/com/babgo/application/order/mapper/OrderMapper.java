package com.babgo.application.order.mapper;

import com.babgo.domain.order.OrderStatus;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderMapper {

    public static Sort toSort(String sortType) {
        //TODO 추후 case를 통해 다양한 정렬 기준 추가 가능
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }


    public static OrderStatus toStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) return OrderStatus.CONFIRMED;

        String normalized = statusStr.trim().toUpperCase().replace("-", "_");
        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID, "올바르지 않은 주문 상태입니다.");
        }
    }
}
