package com.babgo.repository.redis.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class SearchRedisKeyFactory  {

    public static String getCategoryRegionSortCache(String regionCode, String categoryId, String sort) {

        return  String.format("search:zset:%s:%s:%s", regionCode, categoryId, sort);
    }
}
