package com.babgo.repository.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.babgo.MockTest;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchSort;
import com.babgo.domain.search.SearchType;
import com.babgo.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, SearchRepositoryImpl.class})
public class SearchRepositoryTest extends MockTest {


    private double radiusMeters = 2000.0;

    @Autowired
    private SearchRepositoryImpl searchQueryRepository;

    @PersistenceContext
    private EntityManager em;


    private double latitude = 37.5665;
    private double longitude = 126.9780;
    private SearchType searchType;
    private String regionCode = "11110";
    private String keyword = "치킨";
    private String sort = "DISTANCE";    // 정렬 기준 (거리순)
    private int page = 1;
    private int size = 10;

    private UUID chickenCategoryId;
    private UUID pizzaCategoryId;
    private UUID commonCategoryId;

    @BeforeEach
    void setup() {
        chickenCategoryId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        pizzaCategoryId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        commonCategoryId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        List<Search> searches = List.of(
            Search.of(UUID.randomUUID(), regionCode, "치킨나라", chickenCategoryId, "치킨", 4.8, 10, 152, 3, "OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), regionCode, "피자나라치킨공주", chickenCategoryId, "치킨", 4.5, 10, 98, 2, "OPEN", 37.5666, 126.9781),
            Search.of(UUID.randomUUID(), regionCode, "둘둘치킨", chickenCategoryId, "치킨", 4.6, 10,210, 1, "OPEN", 37.5667, 126.9782),

            Search.of(UUID.randomUUID(), regionCode, "피자나라", pizzaCategoryId, "피자", 4.3, 10,120, 4,  "OPEN", 37.5668, 126.9801),
            Search.of(UUID.randomUUID(), regionCode, "피자스쿨", pizzaCategoryId, "피자", 4.1, 10,60, 2,  "OPEN", 37.5669, 126.9798),

            Search.of(UUID.randomUUID(), regionCode, "가까운가게", commonCategoryId, "치킨", 4.8, 152, 1,  10,"OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), regionCode, "중간가게", commonCategoryId, "치킨", 4.5, 98, 1,  10,"OPEN", 37.5710, 126.9780),
            Search.of(UUID.randomUUID(), regionCode, "먼가게", commonCategoryId, "치킨", 4.6, 210, 1,  10,"OPEN", 37.5765, 126.9830),

            Search.of(UUID.randomUUID(), regionCode, "가게좋아요적음", commonCategoryId, "치킨", 4.8, 50, 2,  10,"OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), regionCode, "가게좋아요중간", commonCategoryId, "치킨", 4.6, 150, 1,  10,"OPEN", 37.5665, 126.9780),
            Search.of(UUID.randomUUID(), regionCode, "가게좋아요많음", commonCategoryId, "치킨", 4.5, 300, 3,  10,"OPEN", 37.5665, 126.9780)
        );

        searches.forEach(em::persist); // 영속성 컨텍스트에 저장
        em.flush(); // 지연된 sql을 실제 db에 반영
        em.clear(); // 1차 캐시 초기화
    }



    @Test
    void 카테고리ID검색_정렬기준_거리순_검증_성공() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), chickenCategoryId.toString(), sort, page, size);


        // when: 치킨 카테고리로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then: 결과 검증
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(search -> search.getCategoryId().equals(chickenCategoryId));
        assertThat(results).noneMatch(search -> search.getCategoryId().equals(pizzaCategoryId));


    }


    @Test
    void 가게명_부분_검색_정렬기준_거리순_검증_성공() {

        // given: 특정 가게명 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.STORE.name(), keyword, sort, page, size);

        // when: 치킨이 들어간 가게명 검색
        List<Search> results = searchQueryRepository.getStoreSearch(command, radiusMeters);

        // then: 결과 검증
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Search::getStoreName).containsExactlyInAnyOrder("치킨나라", "피자나라치킨공주", "둘둘치킨");
    }


    @Test
    void 카테고리ID검색_정렬기준_비슷한_거리순_검증_성공() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), commonCategoryId.toString(), sort, page, size);

        // when: 공통 카테고리 및 거리순 정렬 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then: 거리순으로 정렬되었는지 검증
        assertThat(results).hasSize(6);
        assertThat(results).extracting(Search::getStoreName)
            .containsExactly("가까운가게", "가게좋아요적음", "가게좋아요중간", "가게좋아요많음", "중간가게", "먼가게");
    }


    @Test
    void 카테고리ID검색_평점순_정렬_검증_성공() {


        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), commonCategoryId.toString(),
            SearchSort.RATING.name(), page, size);

        // when: 공통 카테고리, 평점순으로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);


        // then: 평점 높은 순으로 정렬되었는지 검증
        assertThat(results).hasSize(6);
        assertThat(results.get(0).getAvgRating()).isEqualTo(4.8);
        assertThat(results.get(1).getAvgRating()).isEqualTo(4.8);
        assertThat(results.get(2).getAvgRating()).isEqualTo(4.6);
        assertThat(results.get(3).getAvgRating()).isEqualTo(4.6);
        assertThat(results.get(4).getAvgRating()).isEqualTo(4.5);
        assertThat(results.get(5).getAvgRating()).isEqualTo(4.5);
    }

    @Test
    void 카테고리ID검색_좋아요순_정렬_검증_성공() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), commonCategoryId.toString(),
            SearchSort.LIKES.name(), page, size);

        // when: 공통 카테고리, 사용자 좋아요순으로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then: 좋아요 많은 순으로 정렬되었는지 검증
        assertThat(results).hasSize(6);
        assertThat(results.get(0).getLikes()).isEqualTo(300);
        assertThat(results.get(1).getLikes()).isEqualTo(210);
        assertThat(results.get(2).getLikes()).isEqualTo(152);
        assertThat(results.get(3).getLikes()).isEqualTo(150);
        assertThat(results.get(4).getLikes()).isEqualTo(98);
        assertThat(results.get(5).getLikes()).isEqualTo(50);
    }

    @Test
    void 카테고리ID검색_가게_최신_생성순_정렬_검증_성공() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), commonCategoryId.toString(),
            SearchSort.CREATED.name(), page, size);


        // when: 공통 카테고리, 가게 생성순으로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then: 가게 생성 최신순으로 검증
        assertThat(results).hasSize(6);
        assertThat(results).isNotEmpty();
        assertThat(results).isSortedAccordingTo(Comparator.comparing(Search::getCreatedAt).reversed());

        assertThat(results.get(0).getCreatedAt()).isAfterOrEqualTo(results.get(1).getCreatedAt());
        assertThat(results.get(1).getCreatedAt()).isAfterOrEqualTo(results.get(2).getCreatedAt());
        assertThat(results.get(2).getCreatedAt()).isAfterOrEqualTo(results.get(3).getCreatedAt());
        assertThat(results.get(3).getCreatedAt()).isAfterOrEqualTo(results.get(4).getCreatedAt());
        assertThat(results.get(4).getCreatedAt()).isAfterOrEqualTo(results.get(5).getCreatedAt());
    }


    @Test
    void 카테고리ID검색_가게_주문순_정렬_검증_성공() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), commonCategoryId.toString(),
            SearchSort.ORDER_COUNT.name(), page, size);


        // when: 공통 카테고리, 주문 많은 순으로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then: 가게 주문 많은 순으로 검색
        assertThat(results).hasSize(6);
        assertThat(results.get(0).getOrderCount()).isEqualTo(3);
        assertThat(results.get(1).getOrderCount()).isEqualTo(2);
        assertThat(results.get(2).getOrderCount()).isEqualTo(1);
        assertThat(results.get(3).getOrderCount()).isEqualTo(1);
        assertThat(results.get(4).getOrderCount()).isEqualTo(1);
        assertThat(results.get(5).getOrderCount()).isEqualTo(1);
    }


    @Test
    void 카테고리ID검색_지역코드불일치_빈결과반환() {

        // given: 카테고리 검색 요청 생성
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, "111120", SearchType.KATEGORIE.name(), commonCategoryId.toString(),
            SearchSort.ORDER_COUNT.name(), page, size);


        // when: 공통 카테고리, 해당 지역코드로 검색
        List<Search> results = searchQueryRepository.getCategorySearch(command, radiusMeters);

        // then:결과 없음 검증
        assertThat(results).hasSize(0);
        assertThat(results).isNotNull().isEmpty();

    }
}


