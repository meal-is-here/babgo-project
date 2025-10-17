package com.babgo.repository.redis.search;

import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getCategoryRegionSortCache;

import com.babgo.domain.search.SearchCache;
import com.babgo.domain.search.SearchCommand.Create;
import com.babgo.domain.search.SearchRedisRepository;
import com.babgo.domain.search.SearchSort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchRedisRepositoryImpl implements SearchRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchRedisRepositoryImpl(@Qualifier("searchRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }




    @Override
    public List<SearchCache> getCategoryRegionCache(Create searchCommand, double radiusMeters) {

        // 키 가져오기
        String key = getCategoryRegionSortCache(searchCommand.getRegionCode(), searchCommand.getKeyword(), searchCommand.getSort());

        List<SearchCache> results = new ArrayList<>();

        int page = Math.max(1, searchCommand.getPage());
        int size = Math.max(1, searchCommand.getSize());
        int start = (page - 1) * size;
        int end = start + size - 1;

        // score 내림차순으로 해당 구간 조회
        Set<String> jsonSet;

        if (SearchSort.DISTANCE.name().equals(searchCommand.getSort())) {

            // 사용자의 현재 위치를 기준으로, 반경(radiusMeters) 내의 데이터를 찾는다
            Circle within = new Circle(new Point(searchCommand.getLongitude(), searchCommand.getLatitude()), new Distance(radiusMeters, Metrics.METERS));

            // Redis GEO 사용해서 반경 내 가게들을 거리순(오름차순)으로 가져옴
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
                redisTemplate.opsForGeo()
                    .radius(key, within, RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates() // 결과에 위도/경도 포함
                        .includeDistance()    // 결과에 거리값 포함
                        .sortAscending()      // 거리 기준 오름차순 정렬 (가까운 순)
                        .limit(searchCommand.getSize()) // 최대 결과 개수 제한
                    );

            if (geoResults != null) {

                // 조회 결과 SearchCache 객체 리스트로 변환
                results = geoResults.getContent().stream()
                    .map(result -> {
                        try {
                            // geo location의 name에 JSON이 들어 있음
                            return objectMapper.readValue(result.getContent().getName(),
                                SearchCache.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Geo 캐시 역직렬화 실패", e);
                        }
                    })
                    .toList();
            }

        } else {
            jsonSet = redisTemplate.opsForZSet().reverseRange(key, start, end); // 내림차순

            if (jsonSet == null) {
                return List.of();
            }

            results = jsonSet.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, SearchCache.class);
                    } catch (JsonProcessingException e) {
                        log.error("redis 역직렬화 실패: {}", e.getMessage(), e);
                        throw new RuntimeException();
                    }
                })
                .toList();

            return results;
        }

        log.info("redis 캐시 조회 성공 key: {}, page: {}, size: {}, 결과 수: {}", key, page, size, results.size());

        return results;

    }

    @Override
    public void saveStoreCache(SearchCache searchCache) {

        saveCacheBySort(searchCache, SearchSort.DISTANCE);
        saveCacheBySort(searchCache, SearchSort.LIKES);
        saveCacheBySort(searchCache, SearchSort.ORDER_COUNT);
        saveCacheBySort(searchCache, SearchSort.CREATED);

    }



    @Override
    public void saveCacheBySort(SearchCache cache, SearchSort sort) {
        try {

            boolean isGeo = (sort == SearchSort.DISTANCE);

            String key = getCategoryRegionSortCache(cache.getRegionCode(), cache.getCategoryId().toString(), sort.name());

            String json = objectMapper.writeValueAsString(cache);

            if (isGeo) {
                redisTemplate.opsForGeo().add(key, new Point(cache.getLongitude(), cache.getLatitude()), json);
                log.info("GEO 저장 key={}, storeId={}", key, cache.getStoreId());
            } else {
                double score = switch (sort) {
                    case LIKES -> cache.getLikes();
                    case ORDER_COUNT -> cache.getOrderCount();
                    case RATING -> cache.getAvgRating();
                    case CREATED -> System.currentTimeMillis();
                    default -> 0;
                };
                redisTemplate.opsForZSet().add(key, json, score);
                redisTemplate.expire(key, Duration.ofHours(1));
                log.info("ZSET 저장 key={}, storeId={}, sort={}, score={}", key, cache.getStoreId(), sort, score);
            }

        } catch (JsonProcessingException e) {
            log.error("Redis 캐시 저장 실패 sort={}, error={}", sort, e.getMessage());
        }
    }


    @Override
    public void incrementOrderCountCache(UUID storeId, UUID categoryId, String regionCode) {

        try {
            String key = getCategoryRegionSortCache(regionCode, categoryId.toString(), SearchSort.ORDER_COUNT.name());
            redisTemplate.opsForZSet().incrementScore(key, storeId.toString(), 1);
            log.info("Redis 주문 수 캐시 증가: storeId={}, key={}", storeId, key);
        } catch (Exception e) {
            log.error("Redis 주문 수 증가 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public void incrementLikeCountCache(UUID storeId, UUID categoryId, String regionCode) {
        try {
            String key = getCategoryRegionSortCache(regionCode, categoryId.toString(), SearchSort.LIKES.name());
            redisTemplate.opsForZSet().incrementScore(key, storeId.toString(), 1);
            log.info("Redis 좋아요 수 캐시 증가: storeId={}, key={}", storeId, key);
        } catch (Exception e) {
            log.error("Redis 좋아요 수 증가 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updateAverageRatingCache(UUID storeId, UUID categoryId, String regionCode, double averageRatinge) {
        try {
            String key = getCategoryRegionSortCache(regionCode, categoryId.toString(), SearchSort.LIKES.name());

            //  새 평균 평점을 반영
            redisTemplate.opsForZSet().add(key, storeId.toString(), averageRatinge);
            log.info("Redis 평점 캐시 증가: storeId={}, key={}", storeId, key);
        } catch (Exception e) {
            log.error("Redis 평점 갱신 실패: {}", e.getMessage(), e);
        }
    }

}
