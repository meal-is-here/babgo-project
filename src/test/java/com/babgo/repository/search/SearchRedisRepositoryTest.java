package com.babgo.repository.search;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.babgo.MockTest;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchCommand.CreateResult;
import com.babgo.domain.search.SearchSort;
import com.babgo.domain.search.SearchType;
import com.babgo.global.config.RedisConfig;
import com.babgo.repository.redis.search.SearchRedisRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(classes = {RedisConfig.class})
@Import({RedisConfig.class, SearchRedisRepositoryImpl.class})
public class SearchRedisRepositoryTest extends MockTest {


    @Autowired
    @Qualifier("searchRedisTemplate")
    private RedisTemplate<String, String>  redisTemplate;

    @Autowired
    private SearchRedisRepositoryImpl searchRedisRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> sorts = List.of(SearchSort.DISTANCE.name(), SearchSort.CREATED.name(), SearchSort.LIKES.name(), SearchSort.ORDER_COUNT.name(), SearchSort.RATING.name());

    private static final UUID categoryId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID categoryId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");


    private double latitude = 37.5665;
    private double longitude = 126.9780;
    private SearchType searchType;
    private String regionCode = "11110";
    private int page = 1;
    private int size = 10;

    @Test
    void testRedisConnection() {
        redisTemplate.opsForValue().set("ping", "pong");
        redisTemplate.opsForValue().get("ping");
    }


    @BeforeEach
    void setUp() throws Exception {
        redisTemplate.getConnectionFactory().getConnection().flushAll();


        // 카테고리별 10개 가게 생성
        List<Map<String, Object>> categoryAStores = createStores(categoryId1, "한식");
        List<Map<String, Object>> categoryBStores = createStores(categoryId2, "양식");

        // 각 카테고리 + 정렬 기준별로 Redis에 저장
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
                Map.entry("likes", (int)(Math.random() * 200)),
                Map.entry("orderCount", (int)(Math.random() * 100)),
                Map.entry("storeStatus", "OPEN"),
                Map.entry("latitude", 37.5700 + (Math.random() * 0.002)),
                Map.entry("longitude", 126.9810 + (Math.random() * 0.002))
            ))
            .collect(Collectors.toList());
    }

    private void saveSortedCaches(List<Map<String, Object>> stores, UUID categoryId) throws JsonProcessingException {
        for (String sort : sorts) {
            // ZSET Key 생성
            String key = String.format("search:zset:%s:%s:%s", regionCode, categoryId, sort);

            // 기존 데이터 삭제 (중복 방지)
            redisTemplate.delete(key);

            for (Map<String, Object> store : stores) {
                // JSON 직렬화 (저장 데이터)
                String json = objectMapper.writeValueAsString(store);

                //  정렬 기준에 따라 score 부여
                double score = switch (sort) {
                    case "LIKES" -> ((Number) store.get("likes")).doubleValue();
                    case "ORDER_COUNT" -> ((Number) store.get("orderCount")).doubleValue();
                    case "RATING" -> ((Number) store.get("avgRating")).doubleValue();
                    case "DISTANCE" -> ((Number) store.get("latitude")).doubleValue();
                    case "CREATED" -> System.currentTimeMillis(); // 최신순
                    default -> 0;
                };

                // ZSET에 (JSON, score) 저장
                redisTemplate.opsForZSet().add(key, json, score);
            }

        }
    }


    @Test
    void 카테고리정렬_캐시조회_좋아요순_정상작동_검증() {
        // given: 좋아요순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.LIKES.name(),
            page, size
        );

        // when: 캐시에서 해당 카테고리 데이터 조회
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 결과 존재 및 좋아요 내림차순 정렬 검증
        assertThat(results != null && !results.isEmpty()).isTrue();
        assertTrue(results.stream().map(CreateResult::getCategoryId).anyMatch(id -> id.equals(categoryId1)));
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getLikes() >= results.get(i).getLikes());
        }
    }

    @Test
    void 카테고리정렬_캐시조회_주문순_정상작동_검증() {
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
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 결과 존재 및 주문수 내림차순 정렬 검증
        assertThat(results != null && !results.isEmpty()).isTrue();
        assertTrue(results.stream().map(CreateResult::getCategoryId).anyMatch(id -> id.equals(categoryId1)));
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getOrderCount() >= results.get(i).getOrderCount());
        }

    }

    @Test
    void 카테고리정렬_캐시조회_평점순_정상작동_검증() {
        // given: 평점순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.RATING.name(),
            page, size
        );

        // when: 캐시에서 해당 카테고리 데이터 조회
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 결과 존재 및 평점 내림차순 정렬 검증
        assertThat(results != null && !results.isEmpty()).isTrue();
        assertTrue(results.stream().map(CreateResult::getCategoryId).anyMatch(id -> id.equals(categoryId1)));
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getAvgRating() >= results.get(i).getAvgRating());
        }    }

    @Test
    void 카테고리정렬_캐시조회_거리순_정상작동_검증() {
        // given: 거리순 정렬 기준으로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.DISTANCE.name(),
            page, size
        );

        // when: 캐시에서 해당 카테고리 데이터 조회
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 결과 존재 및 거리(위도 기준) 오름차순 정렬 검증
        assertThat(results != null && !results.isEmpty()).isTrue();
        assertTrue(results.stream().map(CreateResult::getCategoryId).anyMatch(id -> id.equals(categoryId1)));
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getLatitude() <= results.get(i).getLatitude());
        }    }

    @Test
    void 카테고리정렬_캐시조회_지역코드불일치_빈결과_검증() {
        // given: 존재하지 않는 지역코드로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            "99999",
            SearchType.KATEGORIE.name(),
            categoryId1.toString(),
            SearchSort.LIKES.name(),
            page, size
        );

        // when: 캐시에서 데이터 조회
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 빈 결과 검증
        assertThat(results).isNotNull();
        assertThat(results.isEmpty()).isTrue();
    }

    @Test
    void 카테고리정렬_캐시조회_잘못된카테고리_빈결과_검증() {
        // given: 존재하지 않는 카테고리로 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude,
            regionCode,
            SearchType.KATEGORIE.name(),
            UUID.randomUUID().toString(), // 잘못된 카테고리
            SearchSort.LIKES.name(),
            page, size
        );

        // when: 캐시에서 데이터 조회
        List<CreateResult> results = searchRedisRepository.getCategoryRegionCache(command);

        // then: 빈 결과 검증
        assertThat(results).isNotNull();
        assertThat(results.isEmpty()).isTrue();
    }
}


