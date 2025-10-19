package com.babgo.repository.search;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.babgo.MockTest;
import com.babgo.domain.common.ActionType;
import com.babgo.domain.search.SearchCache;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchSort;
import com.babgo.domain.search.SearchType;
import com.babgo.repository.redis.search.SearchRedisKeyFactory;
import com.babgo.repository.redis.search.SearchRedisRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.domain.geo.Metrics;

@SpringBootTest
public class SearchRedisRepositoryTest extends MockTest {


    @Autowired
    @Qualifier("searchRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SearchRedisRepositoryImpl searchRedisRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> sorts = List.of(SearchSort.DISTANCE.name(),
        SearchSort.CREATED.name(), SearchSort.LIKES.name(), SearchSort.ORDER_COUNT.name(),
        SearchSort.RATING.name());

    private static final UUID categoryId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID categoryId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");


    private double latitude = 37.5665;
    private double longitude = 126.9780;
    private SearchType searchType;
    private String regionCode = "11110";
    private int page = 1;
    private int size = 10;
    public double radiusMeters = 2000.0;


    private String zsetKey(SearchSort sort, UUID categoryId) {
        return SearchRedisKeyFactory.getZsetKey(regionCode, categoryId.toString(), sort.name());
    }

    private String listKey(SearchSort sort, UUID categoryId) {
        return SearchRedisKeyFactory.getListKey(regionCode, categoryId.toString(), sort.name());
    }

    private String hashKey(UUID categoryId) {
        return SearchRedisKeyFactory.getHashKey(regionCode, categoryId.toString());
    }

    private String geoKey(SearchSort sort, UUID categoryId) {
        return SearchRedisKeyFactory.getGeoKey(regionCode, categoryId.toString(), sort.name());
    }


    @BeforeEach
    void setUp() throws Exception {
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());

        List<Map<String, Object>> categoryAStores = createStores(categoryId1, "한식");
        List<Map<String, Object>> categoryBStores = createStores(categoryId2, "양식");

        saveSortedCaches(categoryAStores, categoryId1);
        saveSortedCaches(categoryBStores, categoryId2);


    }

    private List<Map<String, Object>> createStores(UUID categoryId, String categoryName) {
        return IntStream.rangeClosed(1, 10)
            .mapToObj(i -> Map.<String, Object>ofEntries(
                Map.entry("storeId", UUID.randomUUID().toString()),
                Map.entry("regionCode", regionCode),
                Map.entry("storeName", categoryName + "가게" + i),
                Map.entry("categoryId", categoryId.toString()),
                Map.entry("categoryName", categoryName),
                Map.entry("avgRating", 4.0 + Math.random()),
                Map.entry("likeCount", 10),
                Map.entry("likes", (int) (Math.random() * 200)),
                Map.entry("orderCount", (int) (Math.random() * 100)),
                Map.entry("storeStatus", "OPEN"),
                Map.entry("latitude", latitude + (Math.random() * 0.002)),
                Map.entry("longitude", longitude + (Math.random() * 0.002))
            ))
            .collect(Collectors.toList());
    }


    private void saveSortedCaches(List<Map<String, Object>> stores, UUID categoryId)
        throws JsonProcessingException {
        for (SearchSort sort : SearchSort.values()) {

            String zsetKey = zsetKey(sort, categoryId);
            String hashKey = hashKey(categoryId);
            String geoKey = geoKey(sort, categoryId);

            // 기존 캐시 삭제
            redisTemplate.delete(zsetKey);
            redisTemplate.delete(hashKey);
            redisTemplate.delete(geoKey);

            for (Map<String, Object> store : stores) {
                String json = objectMapper.writeValueAsString(store);
                String storeId = (String) store.get("storeId");

                // 정렬 기준별 score 계산
                double score = switch (sort) {
                    case LIKES -> ((Number) store.get("likes")).doubleValue();
                    case ORDER_COUNT -> ((Number) store.get("orderCount")).doubleValue();
                    case RATING -> ((Number) store.get("avgRating")).doubleValue();
                    case DISTANCE -> ((Number) store.get("latitude")).doubleValue();
                    case CREATED -> System.currentTimeMillis();
                };

                if (sort == SearchSort.DISTANCE) {
                    redisTemplate.opsForGeo().add(
                        geoKey,
                        new Point((Double) store.get("longitude"), (Double) store.get("latitude")),
                        storeId
                    );
                } else {
                    redisTemplate.opsForZSet().add(zsetKey, storeId, score);
                    redisTemplate.opsForHash().put(hashKey, storeId, json);
                }
            }
        }
    }

    @Test
    void testRedisConnection() {
        redisTemplate.opsForValue().set("ping", "pong");
        redisTemplate.opsForValue().get("ping");
    }


    @Test
    void 카테고리정렬_캐시조회_좋아요순_정상작동_검증() throws JsonProcessingException {
        // given: 좋아요순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.LIKES.name(),
            page, size
        );

        // when: Redis 캐시에서 해당 카테고리 데이터 조회
        List<SearchCache.Result> results = searchRedisRepository.getCacheByCategory(command, radiusMeters);

        // then: Redis의 ZSET 정렬 순서와 결과 리스트가 동일해야 함
        Set<String> redisRankedStoreIds = redisTemplate.opsForZSet().reverseRange(zsetKey(SearchSort.LIKES, categoryId1), 0, results.size() - 1);

        // then: Redis에서 가장 높은 score(좋아요 수) 가진 storeId 추출 하여 존재 및 좋아요 내림차순 정렬 검증
        Set<TypedTuple<String>> top = redisTemplate.opsForZSet().reverseRangeWithScores(zsetKey(SearchSort.LIKES, categoryId1), 0, 0);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        System.out.println("Redis 순위: " + redisRankedStoreIds);
        System.out.println("결과 리스트(JSON): \n" + writer.writeValueAsString(results));


        assertEquals(
            redisRankedStoreIds.size(), results.size(),
            "Redis의 정렬 결과와 조회된 결과 개수가 일치해야 함"
        );

        Assertions.assertNotNull(top);
        String expectedTopStoreId = top.iterator().next().getValue();
        String actualTopStoreId = results.get(0).getStoreId().toString();

        assertEquals(expectedTopStoreId, actualTopStoreId, "Redis ZSET에서 좋아요순 1위 가게와 결과의 첫 번째 가게가 일치해야 함");
    }


    @Test
    void 카테고리정렬_캐시조회_주문순_정상작동_검증() throws JsonProcessingException {
        // given: 주문많은순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.ORDER_COUNT.name(),
            page, size
        );

        // when: 캐시에서 해당 카테고리 데이터 조회
        List<SearchCache.Result> results = searchRedisRepository.getCacheByCategory(command, radiusMeters);

        // then: Redis ZSET의 실제 정렬 순서와 조회 결과의 순서가 동일해야 함
        Set<String> redisRankedStoreIds = redisTemplate.opsForZSet().reverseRange(zsetKey(SearchSort.ORDER_COUNT, categoryId1), 0, results.size() - 1);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        System.out.println("Redis 순위: " + redisRankedStoreIds);
        System.out.println("결과 리스트(JSON): \n" + writer.writeValueAsString(results));


        // Redis와 결과 리스트의 크기가 동일해야 함
        assertEquals(redisRankedStoreIds.size(), results.size(), "Redis의 정렬 결과와 조회된 결과 개수가 일치해야 함");

        // Redis 순서와 결과 순서가 정확히 일치해야 함
        List<String> redisOrderList = new ArrayList<>(redisRankedStoreIds);
        List<String> resultOrderList = results.stream()
            .map(r -> r.getStoreId().toString())
            .toList();

        for (int i = 0; i < resultOrderList.size(); i++) {
            assertEquals(redisOrderList.get(i), resultOrderList.get(i),
                String.format("순위 불일치: index=%d", i));
        }

    }

    @Test
    void 카테고리정렬_캐시조회_평점순_정상작동_검증() throws JsonProcessingException {
        // given: 평점순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.RATING.name(),
            page, size
        );

        // when: Redis 캐시에서 해당 카테고리 데이터 조회
        List<SearchCache.Result> results = searchRedisRepository.getCacheByCategory(command, radiusMeters);

        // then: Redis ZSET의 실제 정렬 순서와 결과 리스트가 동일해야 함
        Set<String> redisRankedStoreIds = redisTemplate.opsForZSet()
            .reverseRange(zsetKey(SearchSort.RATING, categoryId1), 0, results.size() - 1);


        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        System.out.println("결과 리스트(JSON): \n" + writer.writeValueAsString(results));

        // Redis와 결과 리스트의 크기가 동일해야 함
        assertEquals(
            redisRankedStoreIds.size(), results.size(),
            "Redis의 평점순 정렬 결과와 조회된 결과 개수가 일치해야 함"
        );

        // Redis 순서와 결과 순서가 정확히 일치해야 함
        List<String> redisOrderList = new ArrayList<>(redisRankedStoreIds);
        List<String> resultOrderList = results.stream()
            .map(r -> r.getStoreId().toString())
            .toList();

        for (int i = 0; i < resultOrderList.size(); i++) {
            assertEquals(
                redisOrderList.get(i), resultOrderList.get(i),
                String.format("평점순 순위 불일치 발생 (index=%d)", i)
            );
        }

        System.out.println("Redis 평점순과 결과 순서가 정확히 일치합니다.");
    }

    @Test
    void 카테고리정렬_캐시조회_거리순_정상작동_검증() throws JsonProcessingException {
        // given
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.DISTANCE.name(),
            page, size
        );

        // when: Redis 캐시에서 해당 카테고리 데이터 조회
        List<SearchCache.Result> results = searchRedisRepository.getCacheByCategory(command, radiusMeters);

        // then  GEOSEARCH로 거리순 결과 가져와서 순서 검증
        List<String> geoResults = Objects.requireNonNull(redisTemplate.opsForGeo()
                .radius(geoKey(SearchSort.DISTANCE, categoryId1),
                    new Circle(new Point(longitude, latitude),
                        new Distance(radiusMeters, Metrics.METERS))))
            .getContent()
            .stream()
            .map(GeoResult::getContent)
            .map(GeoLocation::getName)
            .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        System.out.println("GEO 결과: " + geoResults);
        System.out.println("결과 리스트(JSON): \n" + writer.writeValueAsString(results));


        assertEquals(geoResults.size(), results.size(), "Redis의 GEO 거리순 결과와 조회 결과 개수가 일치해야 함");

        List<String> resultOrderList = results.stream()
            .map(r -> r.getStoreId().toString())
            .toList();

        for (int i = 0; i < resultOrderList.size(); i++) {
            assertEquals(
                geoResults.get(i), resultOrderList.get(i),
                String.format("거리순 순위 불일치 발생 (index=%d)", i)
            );
        }

        assertFalse(geoResults.isEmpty(), "GEO 데이터가 존재해야 합니다 (거리순 캐시 누락 가능)");

        System.out.println("Redis GEO 기반 거리순 결과와 조회 순서가 정확히 일치합니다.");
    }


    @Test
    void 주문수_증가시_순위리스트_갱신검증() throws JsonProcessingException {
        // given
        UUID categoryId = categoryId1;
        SearchSort sort = SearchSort.ORDER_COUNT;
        Map<String, String> keys = SearchRedisRepositoryImpl.getAllKeys(regionCode, categoryId.toString(), sort.name());
        String zsetKey = keys.get("zsetKey");
        String listKey = keys.get("listKey");

        // 더미 데이터 10개 삽입
        List<Map<String, Object>> stores = createStores(categoryId, "한식");
        saveSortedCaches(stores, categoryId);

        // 현재 1위 가게 확인
        String beforeTop = redisTemplate.opsForList().index(listKey, 0);
        System.out.println("증가 전 1위: " + beforeTop);

        // when: 5번째 가게(orderCount +100)
        String targetStoreId = (String) stores.get(4).get("storeId");
        SearchCache.CountUpdate update = SearchCache.CountUpdate.builder()
            .storeId(UUID.fromString(targetStoreId))
            .categoryId(categoryId)
            .sort(sort)
            .actionType(ActionType.CREATE)
            .key(SearchCache.Key.builder()
                .regionCode(regionCode)
                .categoryId(categoryId.toString())
                .sort(sort)
                .build())
            .build();

        for (int i = 0; i < 100; i++) {
            searchRedisRepository.incrementOrderCountCache(update, sort);
        }

        // then
        String afterTop = redisTemplate.opsForList().index(listKey, 0);
        System.out.println("증가 후 1위: " + afterTop);

        assertNotEquals(beforeTop, afterTop, "주문 증가 후 1위 가게가 변경되어야 함");
    }

    @Test
    void 평점_등록_수정_삭제_순위_정상갱신_검증() throws JsonProcessingException {
        // given
        SearchSort sort = SearchSort.RATING;
        UUID categoryId = categoryId1;

        // Redis에 테스트용 데이터 삽입
        List<Map<String, Object>> stores = createStores(categoryId, "한식");
        saveSortedCaches(stores, categoryId);

        // 첫 번째 가게 선택
        String targetStoreId = (String) stores.get(0).get("storeId");
        String zsetKey = SearchRedisKeyFactory.getZsetKey(regionCode, categoryId.toString(), sort.name());

        Double before = redisTemplate.opsForZSet().score(zsetKey, targetStoreId);
        System.out.println("초기 평점: " + before);

        // 1️평점 등록 (5점 추가)
        SearchCache.Update create = SearchCache.Update.builder()
            .storeId(UUID.fromString(targetStoreId))
            .newRating(5)
            .oldRating(0)
            .actionType(ActionType.CREATE)
            .key(SearchCache.Key.builder()
                .regionCode(regionCode)
                .categoryId(categoryId.toString())
                .sort(sort)
                .build())
            .build();

        searchRedisRepository.changeAverageRatingCache(create, sort);
        Double afterCreate = redisTemplate.opsForZSet().score(zsetKey, targetStoreId);
        System.out.println("등록 후 평점: " + afterCreate);
        assertTrue(afterCreate > before);

        // 2️평점 수정 (5 → 3점)
        SearchCache.Update update = SearchCache.Update.builder()
            .storeId(UUID.fromString(targetStoreId))
            .newRating(3)
            .oldRating(5)
            .actionType(ActionType.UPDATE)
            .key(SearchCache.Key.builder()
                .regionCode(regionCode)
                .categoryId(categoryId.toString())
                .sort(sort)
                .build())
            .build();

        searchRedisRepository.changeAverageRatingCache(update, sort);
        Double afterUpdate = redisTemplate.opsForZSet().score(zsetKey, targetStoreId);
        System.out.println("수정 후 평점: " + afterUpdate);
        assertTrue(afterUpdate < afterCreate);

        // 3️평점 삭제 (3점 제거)
        SearchCache.Update cancel = SearchCache.Update.builder()
            .storeId(UUID.fromString(targetStoreId))
            .newRating(0)
            .oldRating(3)
            .actionType(ActionType.CANCEL)
            .key(SearchCache.Key.builder()
                .regionCode(regionCode)
                .categoryId(categoryId.toString())
                .sort(sort)
                .build())
            .build();

        searchRedisRepository.changeAverageRatingCache(cancel, sort);
        Double afterCancel = redisTemplate.opsForZSet().score(zsetKey, targetStoreId);
        System.out.println("삭제 후 평점: " + afterCancel);
        assertNotEquals(afterUpdate, afterCancel);
    }

}



