package com.babgo.repository.redis.search;

import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getGeoKey;
import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getHashKey;
import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getListKey;
import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getZsetKey;

import com.babgo.domain.common.ActionType;
import com.babgo.domain.search.SearchCache;
import com.babgo.domain.search.SearchCommand.Create;
import com.babgo.domain.search.SearchRedisRepository;
import com.babgo.domain.search.SearchSort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchRedisRepositoryImpl implements SearchRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DefaultRedisScript<String> updateStoreCacheScript;

    public SearchRedisRepositoryImpl(
        @Qualifier("searchRedisTemplate") RedisTemplate<String, String> redisTemplate,
        DefaultRedisScript<String> updateStoreCacheScript) {
        this.redisTemplate = redisTemplate;
        this.updateStoreCacheScript = updateStoreCacheScript;
    }

    public List<SearchCache.Result> getCacheByCategory(Create searchCommand, double radiusMeters) {
        String hashKey = getHashKey(searchCommand.getRegionCode(), searchCommand.getKeyword());
        SearchSort sort = searchCommand.getSort();

        int page = Math.max(1, searchCommand.getPage());
        int size = Math.max(1, searchCommand.getSize());
        int start = (page - 1) * size;
        int end = start + size - 1;

        //  거리순은 GEO 기반 조회
        if (sort == SearchSort.DISTANCE) {
            String geoKey = getGeoKey(searchCommand.getRegionCode(), searchCommand.getKeyword(), sort.name());
            GeoResults<GeoLocation<String>> geoResults =
                redisTemplate.opsForGeo().radius(
                    geoKey,
                    new Circle(new Point(searchCommand.getLongitude(), searchCommand.getLatitude()),
                        new Distance(radiusMeters, Metrics.METERS))
                );

            if (geoResults == null || geoResults.getContent().isEmpty()) {
                log.info("GEO 데이터 없음 - key={}", geoKey);
                return List.of();
            }

            // GEO 결과에서 storeId 추출
            List<String> storeIds = geoResults.getContent().stream()
                .map(GeoResult::getContent)
                .map(GeoLocation::getName)
                .toList();

            // HASH에서 JSON 조회
            List<Object> jsonList = redisTemplate.opsForHash().multiGet(hashKey, new ArrayList<>(storeIds));
            log.info("조회된 GEO JSON 개수 = {}", jsonList.size());

            return jsonList.stream()
                .filter(Objects::nonNull)
                .map(json -> {
                    try {
                        return objectMapper.readValue((String) json, SearchCache.Result.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Redis 역직렬화 실패", e);
                    }
                })
                .toList();
        } else {
        // 그 외 정렬은 기존 ZSET + HASH 로직 유지
        String zsetKey = getZsetKey(searchCommand.getRegionCode(), searchCommand.getKeyword(), sort.name());
        Set<String> storeIds = redisTemplate.opsForZSet().reverseRange(zsetKey, start, end);
        log.info("조회된 storeIds = {}", storeIds);

        if (storeIds == null || storeIds.isEmpty()) {
            return List.of();
        }

        List<Object> jsonList = redisTemplate.opsForHash().multiGet(hashKey, new ArrayList<>(storeIds));
        log.info("조회된 JSON 개수 = {}", jsonList.size());

        return jsonList.stream()
            .filter(Objects::nonNull)
            .map(json -> {
                try {
                    return objectMapper.readValue((String) json, SearchCache.Result.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Redis 역직렬화 실패", e);
                }
            })
            .toList();

        }

    }

    @Override
    public void saveStoreCache(SearchCache.Create searchCache) {

        saveCacheBySort(searchCache, SearchSort.DISTANCE);
        saveCacheBySort(searchCache, SearchSort.LIKES);
        saveCacheBySort(searchCache, SearchSort.ORDER_COUNT);
        saveCacheBySort(searchCache, SearchSort.CREATED);

    }


    @Override
    public void saveCacheBySort(SearchCache.Create cache, SearchSort sort) {
        try {

            boolean isGeo = (sort == SearchSort.DISTANCE);

            Map<String, String> keys = getAllKeys(cache.getRegionCode(),
                cache.getCategoryId().toString(), sort.name());

            String zsetKey = keys.get("zsetKey");
            String hashKey = keys.get("hashKey");
            String listKey = keys.get("listKey");
            String geoKey = keys.get("geoKey");

            String json = objectMapper.writeValueAsString(cache);

            if (isGeo) {
                redisTemplate.opsForGeo().add(geoKey, new Point(cache.getLongitude(), cache.getLatitude()), cache.getStoreId().toString());
            } else {
                double score = switch (sort) {
                    case LIKES -> cache.getLikes();
                    case ORDER_COUNT -> cache.getOrderCount();
                    case RATING -> cache.getAvgRating();
                    case CREATED -> System.currentTimeMillis();
                    default -> 0;
                };

                // ZSET + HASH 기본 저장
                redisTemplate.opsForZSet().add(zsetKey, cache.getStoreId().toString(), score);
                redisTemplate.opsForHash().put(hashKey, cache.getStoreId().toString(), json);

                // Lua로 LIST 초기 정렬 갱신
                executeLuaScript(zsetKey, hashKey, listKey, cache.getStoreId().toString(), sort.name(), "INIT", 0, cache.getLatitude(), cache.getLongitude());

            }

        } catch (JsonProcessingException e) {
            log.error("Redis 캐시 저장 실패 sort={}, error={}", sort, e.getMessage());
        }
    }


    @Override
    public void incrementOrderCountCache(SearchCache.CountUpdate cache, SearchSort sort) {

        try {
            Map<String, String> keys = getAllKeys(cache.getKey().getRegionCode(), cache.getCategoryId().toString(), sort.name());
            String zsetKey = keys.get("zsetKey");
            String hashKey = keys.get("hashKey");
            String listKey = keys.get("listKey");

            int delta = cache.getActionType() == ActionType.CREATE ? 1 : -1;
            executeLuaScript(zsetKey, hashKey, listKey, cache.getStoreId().toString(), sort.name(), ActionType.CREATE.name(), delta);

            log.info("Redis 주문 수 갱신 완료: storeId={}, delta={}, key={}", cache.getStoreId(), delta,
                zsetKey);

        } catch (Exception e) {
            log.error("Redis 주문 수 증가 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public void changeLikeCountCache(SearchCache.CountUpdate cache, SearchSort sort) {
        try {
            Map<String, String> keys = getAllKeys(cache.getKey().getRegionCode(), cache.getKey().getCategoryId(), SearchSort.LIKES.name());
            String zsetKey = keys.get("zsetKey");
            String hashKey = keys.get("hashKey");
            String listKey = keys.get("listKey");

            int delta = cache.getActionType() == ActionType.CREATE ? 1 : -1;
            String action = cache.getActionType() == ActionType.CREATE ? "CREATE" : "CANCEL";

            executeLuaScript(zsetKey, hashKey, listKey, cache.getStoreId().toString(), sort.name(), action, delta);

            log.info("Redis 좋아요 캐시 갱신: storeId={}, action={}, delta={}, key={}", cache.getStoreId(),
                action, delta, zsetKey);
        } catch (Exception e) {
            log.error("Redis 좋아요 수 증가 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public void changeAverageRatingCache(SearchCache.Update cache, SearchSort sort) {
        try {
            Map<String, String> keys = getAllKeys(cache.getKey().getRegionCode(), cache.getKey().getCategoryId(), SearchSort.RATING.name());
            String zsetKey = keys.get("zsetKey");
            String hashKey = keys.get("hashKey");
            String listKey = keys.get("listKey");

            double newRating = cache.getNewRating();
            double oldRating = cache.getOldRating();
            String action = cache.getActionType().name();

            executeLuaScript(zsetKey, hashKey, listKey, cache.getStoreId().toString(), sort.name(), action, newRating, oldRating);

            log.info("Redis 평점 갱신 완료: storeId={}, newRating={}, action={}", cache.getStoreId(),
                newRating, action);
        } catch (Exception e) {
            log.error("Redis 평점 갱신 실패: {}", e.getMessage(), e);
        }
    }

    private void executeLuaScript(String zsetKey, String hashKey, String listKey, String storeId,
        String sort, String action, Object... extraArgs) {

        List<Object> args = new ArrayList<>();

        // 기본 인자들 (전부 문자열 변환)
        args.add(String.valueOf(storeId));  // ARGV[1]
        args.add(String.valueOf(sort));     // ARGV[2]

        // delta 값 (없으면 "0")
        args.add(String.valueOf(extraArgs.length > 0 ? extraArgs[0] : "0")); // ARGV[3]

        // 동작 타입 (CREATE, CANCEL, UPDATE 등)
        args.add(String.valueOf(action));   // ARGV[4]

        // 나머지 인자들 (oldRating, latitude, longitude 등)
        for (int i = 1; i < extraArgs.length; i++) {
            args.add(String.valueOf(extraArgs[i]));
        }

        // 모든 인자를 String으로 직렬화해서 Lua 실행
        redisTemplate.execute(updateStoreCacheScript, List.of(zsetKey, hashKey, listKey), args.toArray());


    }

    public static Map<String, String> getAllKeys(String regionCode, String categoryId,
        String sort) {
        Map<String, String> keys = new HashMap<>();
        keys.put("zsetKey", getZsetKey(regionCode, categoryId, sort));
        keys.put("hashKey", getHashKey(regionCode, categoryId));
        keys.put("listKey", getListKey(regionCode, categoryId, sort));
        keys.put("geoKey", getGeoKey(regionCode, categoryId, sort));
        return keys;
    }

}
