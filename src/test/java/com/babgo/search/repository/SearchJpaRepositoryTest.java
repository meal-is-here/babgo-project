package com.babgo.search.repository;

import static org.assertj.core.api.Assertions.assertThat;


import com.babgo.domain.search.Search;
import com.babgo.repository.search.SearchJpaRepository;
import com.babgo.search.MockTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SearchJpaRepositoryTest extends MockTest {


    @Autowired
    private SearchJpaRepository searchJpaRepository;

    private double radiusMeters = 2000.0;


    @Test
    void 카테고리ID_검색_성공() {

        // given: 가게 검색 등록
        UUID chickenCategoryId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID pizzaCategoryId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "치킨나라", chickenCategoryId, "치킨", 4.8, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "치킨세상", chickenCategoryId, "치킨", 4.5, 98,
                "DELIVERY_AVAILABLE", "OPEN", 37.5666, 126.9781),
            Search.of(UUID.randomUUID(), "11110", "둘둘치킨", chickenCategoryId, "치킨", 4.6, 210,
                "DELIVERY_AVAILABLE", "OPEN", 37.5667, 126.9782),
            Search.of(UUID.randomUUID(), "11110", "피자나라", pizzaCategoryId, "피자", 4.3, 120,
                "DELIVERY_AVAILABLE", "OPEN", 37.5668, 126.9801)
        );

        searchJpaRepository.saveAll(searches);

        // when: 치킨 카테고리로 검색
        List<Search> results = searchJpaRepository.getStoresByCategory(
            37.5665, 126.9780, chickenCategoryId.toString(), "DISTANCE", 0, 10, radiusMeters);

        // then: 결과 검증
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3); // 치킨 가게 3개만 반환
        assertThat(results).allMatch(search -> search.getCategoryId().equals(chickenCategoryId));
        assertThat(results).noneMatch(search -> search.getCategoryId().equals(pizzaCategoryId));
    }


    @Test
    void 가게명_LIKE_검색_성공() {
        // given: 다양한 가게명 등록
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "치킨나라", categoryId, "치킨", 4.8, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "맛있는치킨", categoryId, "치킨", 4.5, 98,
                "DELIVERY_AVAILABLE", "OPEN", 37.5670, 126.9792),
            Search.of(UUID.randomUUID(), "11110", "치킨천국", categoryId, "치킨", 4.6, 210,
                "DELIVERY_AVAILABLE", "OPEN", 37.5655, 126.9778),
            Search.of(UUID.randomUUID(), "11110", "피자나라", categoryId, "피자", 4.3, 120,
                "DELIVERY_AVAILABLE", "OPEN", 37.5668, 126.9801),
            Search.of(UUID.randomUUID(), "11110", "한식당", categoryId, "한식", 4.2, 85,
                "DELIVERY_AVAILABLE", "OPEN", 37.5672, 126.9805)
        );

        searchJpaRepository.saveAll(searches);

        // when: "치킨"으로 검색 (LIKE 검색)
        List<Search> results = searchJpaRepository.getStoresByName(
            37.5665, 126.9780, "치킨", "DISTANCE", 0, 10, radiusMeters
        );

        // then: 결과 검증
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Search::getStoreName)
            .containsExactlyInAnyOrder("치킨나라", "치킨천국");
    }

    @Test
    void 거리순_정렬_검증() {

        // given: 거리가 다른 가게들 등록 (중심점: 37.5665, 126.9780)
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "가까운가게", categoryId, "치킨", 4.8, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "중간가게", categoryId, "치킨", 4.5, 98,
                "DELIVERY_AVAILABLE", "OPEN", 37.5710, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "먼가게", categoryId, "치킨", 4.6, 210,
                "DELIVERY_AVAILABLE", "OPEN", 37.5765, 126.9830)
        );

        searchJpaRepository.saveAll(searches);

        // when: 거리순으로 정렬하여 검색
        List<Search> results = searchJpaRepository.getStoresByCategory(
            37.5665, 126.9780, categoryId.toString(), "DISTANCE", 0, 10, radiusMeters
        );

        // then: 거리순으로 정렬되었는지 검증
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStoreName()).isEqualTo("가까운가게");
        assertThat(results.get(1).getStoreName()).isEqualTo("중간가게");
        assertThat(results.get(2).getStoreName()).isEqualTo("먼가게");
    }


    @Test
    void 비슷한_거리순_정렬_검증() {

        // given: 거리가 비슷한 가게 등록
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "10미터치킨", categoryId, "치킨", 4.8, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5666, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "11미터치킨", categoryId, "치킨", 4.7, 130,
                "DELIVERY_AVAILABLE", "OPEN", 37.5666, 126.9781),
            Search.of(UUID.randomUUID(), "11110", "15미터치킨", categoryId, "치킨", 4.6, 120,
                "DELIVERY_AVAILABLE", "OPEN", 37.5667, 126.9781)
        );
        searchJpaRepository.saveAll(searches);

        // when: 거리순으로 정렬하여 검색
        List<Search> results = searchJpaRepository.getStoresByCategory(
            37.5665, 126.9780, categoryId.toString(), "DISTANCE", 0, 10, radiusMeters
        );

        // then: 거리순으로 정렬되었는지 검증
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStoreName()).isEqualTo("10미터치킨");
        assertThat(results.get(1).getStoreName()).isEqualTo("11미터치킨");
        assertThat(results.get(2).getStoreName()).isEqualTo("15미터치킨");
    }


    @Test
    void 평점순_정렬_검증() {
        // given: 평점이 다른 가게들 등록
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "가게저평순", categoryId, "치킨", 3.5, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "가게고평순", categoryId, "치킨", 4.8, 98,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "가게중평순", categoryId, "치킨", 4.2, 210,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780)
        );

        searchJpaRepository.saveAll(searches);

        // when: 평점순으로 정렬하여 검색
        List<Search> results = searchJpaRepository.getStoresByName(
            37.5665, 126.9780, "가게", "RATING", 0, 10, radiusMeters
        );

        // then: 평점 높은 순으로 정렬되었는지 검증
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getAvgRating()).isEqualTo(4.8);
        assertThat(results.get(1).getAvgRating()).isEqualTo(4.2);
        assertThat(results.get(2).getAvgRating()).isEqualTo(3.5);
    }

    @Test
    void 좋아요순_정렬_검증() {
        // given: 좋아요 수가 다른 가게들 등록
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "가게좋아요적음", categoryId, "치킨", 4.8, 50,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "가게좋아요중간", categoryId, "치킨", 4.6, 150,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), "11110", "가게좋아요많음", categoryId, "치킨", 4.5, 300,
                "DELIVERY_AVAILABLE", "OPEN", 37.5665, 126.9780)
        );

        searchJpaRepository.saveAll(searches);

        // when: 좋아요순으로 정렬하여 검색
        List<Search> results = searchJpaRepository.getStoresByName(
            37.5665, 126.9780, "가게", "LIKES", 0, 10, radiusMeters
        );

        // then: 좋아요 많은 순으로 정렬되었는지 검증
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getLikes()).isEqualTo(300);
        assertThat(results.get(1).getLikes()).isEqualTo(150);
        assertThat(results.get(2).getLikes()).isEqualTo(50);
    }

    @Test
    void 반경_제한_검증() {
        // given: 중심점에서 거리가 다른 가게들 등록
        UUID categoryId = UUID.randomUUID();
        double centerLat = 37.5665;
        double centerLng = 126.9780;

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), "11110", "반경내가게", categoryId, "치킨", 4.8, 152,
                "DELIVERY_AVAILABLE", "OPEN", 37.5666, 126.9781),
            Search.of(UUID.randomUUID(), "11110", "반경밖가게", categoryId, "치킨", 4.5, 98,
                "DELIVERY_AVAILABLE", "OPEN", 37.5450, 126.9780)
        );

        searchJpaRepository.saveAll(searches);

        // when: 2km 반경으로 검색
        List<Search> results = searchJpaRepository.getStoresByName(
            centerLat, centerLng, "반경", "DISTANCE", 0, 10, radiusMeters);

        // then: 반경 내 가게만 반환되는지 검증
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStoreName()).isEqualTo("반경내가게");
    }

    @Test
    void 페이징_검증() {

        // given: 15개의 가게 등록 (페이지 크기 10 기준)
        UUID categoryId = UUID.randomUUID();

        List<Search> searches = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            searches.add(
                Search.of(
                    UUID.randomUUID(),
                    "11110",
                    "가게" + i,
                    categoryId,
                    "치킨",
                    4.0 + (i % 5) * 0.1, // 평점 다양하게
                    100 + i * 10, // 좋아요 수 다양하게
                    "DELIVERY_AVAILABLE",
                    "OPEN",
                    37.5665,
                    126.9780
                )
            );
        }

        searchJpaRepository.saveAll(searches);


        // when: 첫 번째 페이지 (page=0, size=10)
        List<Search> firstPage = searchJpaRepository.getStoresByName(
            37.5665, 126.9780, "가게", "DISTANCE", 0, 10, radiusMeters
        );

        // when: 두 번째 페이지 (page=1, size=10)
        List<Search> secondPage = searchJpaRepository.getStoresByName(
            37.5665, 126.9780, "가게", "DISTANCE", 1, 10, radiusMeters
        );

        // then: 페이징이 올바르게 동작하는지 검증
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5); // 남은 5개
        assertThat(firstPage).isNotEqualTo(secondPage); // 서로 다른 페이지여야 함
    }

}


