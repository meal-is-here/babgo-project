package com.babgo.repository.redis.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class SearchRedisKeyFactory  {


    // ZSET 키 생성 (정렬 기준별 점수 저장)
    public static String getZsetKey(String regionCode, String categoryId, String sort) {
        return String.format("search:zset:%s:%s:%s", regionCode, categoryId, sort);
    }


    // HASH 키 생성 (가게 상세 JSON 저장)
    public static String getHashKey(String regionCode, String categoryId) {
        return String.format("search:hash:%s:%s", regionCode, categoryId);
    }

    // LIST 키 생성 (정렬된 JSON 리스트 캐시)
    public static String getListKey(String regionCode, String categoryId, String sort) {
        return String.format("search:list:%s:%s:%s", regionCode, categoryId, sort);
    }

    // GEO 위치정보 저장용 키 생성 (거리 기반 검색용)
    public static String getGeoKey(String regionCode, String categoryId, String sort) {
        return String.format("search:list:%s:%s:%s", regionCode, categoryId, sort);
    }
}
