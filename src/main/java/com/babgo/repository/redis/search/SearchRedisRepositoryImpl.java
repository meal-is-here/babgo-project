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

        log.info("Saving cache for2 {}", searchCache);

        try {
            saveGeoCache(searchCache);
            saveZSetCache(searchCache, SearchSort.LIKES, searchCache.getLikes());
            saveZSetCache(searchCache, SearchSort.ORDER_COUNT, searchCache.getOrderCount());
            saveZSetCache(searchCache, SearchSort.CREATED, searchCache.getOrderCount());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    // 거리순 위치는 변하지 않으니 ttl은 일단 생략
    private void saveGeoCache(SearchCache cache) throws JsonProcessingException {
        String key = getCategoryRegionSortCache(cache.getRegionCode(),
            cache.getCategoryId().toString(), SearchSort.DISTANCE.name());
        redisTemplate.opsForGeo().add(key, new Point(cache.getLongitude(), cache.getLatitude()), objectMapper.writeValueAsString(cache));

    }


    // 좋아요, 주문많은순, 가개생설순
    private void saveZSetCache(SearchCache cache, SearchSort sort, double score)
        throws JsonProcessingException {
        String key = getCategoryRegionSortCache(cache.getRegionCode(),
            cache.getCategoryId().toString(), sort.name());

        redisTemplate.opsForZSet().add(key, objectMapper.writeValueAsString(cache), score);

        redisTemplate.expire(key, Duration.ofHours(1));

        log.info("Redis 저장 결과: key={}, storeId={}, sort={}, score={}", key, cache.getStoreId(), sort, score);
    }


}
