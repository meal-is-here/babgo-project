package com.babgo.search.domain;

import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchRepository;
import com.babgo.domain.search.SearchService;
import com.babgo.search.MockTest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class SearchServiceTest extends MockTest {

    @InjectMocks
    private SearchService  searchService;

    @Mock
    private SearchRepository searchRepository;

    private double latitude = 37.5665;       // 서울 위도
    private double longitude = 126.9780;      // 서울 경도
    private String searchType = "STORE"; // 검색 타입 (예: 가게)
    private String keyword = "치킨";     // 검색 키워드
    private String sort = "DISTANCE";    // 정렬 기준 (거리순)
    private int page = 0;
    private int size = 10;


    @Test
    void 가게_조회_성공(){

        // given: 조회 요청에 사용할 Command 파라미터 준비
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, searchType, keyword, sort, page, size);

        // given: Repository가 반환할 가짜 Search 객체(Mock) 준비
        Search search1 = Mockito.mock(Search.class);
        Search search2 = Mockito.mock(Search.class);

        List<Search> expected = List.of(search1, search2);


        Mockito.when(searchRepository.getStores(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(expected);

        // when: 조회
        List<Search> result = searchService.getSearch(command);

        // then: 결과와 호출 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(expected, result);
        Mockito.verify(searchRepository, Mockito.times(1)).getStores(command, SearchService.DEFAULT_RADIUS_METER);
        Mockito.verifyNoMoreInteractions(searchRepository);

    }

    @Test
    void 가게_조회_결과_없으면_빈값_반환(){

        // given: 조회 요청에 사용할 Command 파라미터 준비 및 조회 시 빈값 세팅
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, searchType, keyword, sort, page, size);
        List<Search> emptyResult = Collections.emptyList();

        Mockito.when(searchRepository.getStores(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(emptyResult);

        // when: 조회
        List<Search> result = searchService.getSearch(command);

        // then: 빈 결과 반환 확인
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
        Mockito.verify(searchRepository, Mockito.times(1)).getStores(command, SearchService.DEFAULT_RADIUS_METER);

    }


    @Test
    void 가게_조회_결과_성공_시_반환_값_10개(){

        // given: 조회 요청에 사용할 Command 파라미터 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, searchType, keyword, sort, page, size
        );

        // given: Search.of()로 검색 결과 10개 생성
        List<Search> mockResult = IntStream.range(0, 10)
            .mapToObj(i -> Search.of(
                UUID.randomUUID(),                      // storeId
                "110" + i,                              // regionCode
                "테스트가게" + i,                         // storeName
                UUID.randomUUID(),                      // categoryId
                "카테고리" + i,                          // categoryName
                4.0 + (i % 5) * 0.1,                    // avgRating
                i * 3,                                  // likes
                "가능",                                  // deliveryStatus
                "영업중",                                // storeStatus
                37.5665 + i * 0.001,                    // latitude
                126.9780 + i * 0.001                    // longitude
            ))
            .collect(Collectors.toList());


        Mockito.when(searchRepository.getStores(command, SearchService.DEFAULT_RADIUS_METER))
            .thenReturn(mockResult);

        // when: 조회
        List<Search> result = searchService.getSearch(command);

        // then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());
        Mockito.verify(searchRepository, Mockito.times(1))
            .getStores(command, SearchService.DEFAULT_RADIUS_METER);

    }

    @Test
    void 검색타입_카테고리_검색_성공() {
        // given: 카테고리 검색 요청 반환할 가짜 Search 객체(Mock) 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, "KATEGORIE", keyword, sort, page, size
        );

        Search search1 = Mockito.mock(Search.class);
        Search search2 = Mockito.mock(Search.class);

        List<Search> expected = List.of(search1, search2);

        Mockito.when(searchRepository.getCategoryStores(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(expected);

        // when: 카테고리 검색 실행
        List<Search> result = searchService.getSearch(command);

        // then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertSame(expected, result);
        Mockito.verify(searchRepository, Mockito.times(1)).getCategoryStores(command, SearchService.DEFAULT_RADIUS_METER);
    }



    @Test
    void 검색타입_가게명_검색_성공() {
        // given: 가게 검색 요청 반환할 가짜 Search 객체(Mock) 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, "STORE", keyword, sort, page, size
        );

        Search search1 = Mockito.mock(Search.class);
        Search search2 = Mockito.mock(Search.class);

        List<Search> expected = List.of(search1, search2);

        Mockito.when(searchRepository.getStores(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(expected);

        // when: 가게 검색 실행
        List<Search> result = searchService.getSearch(command);

        // then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertSame(expected, result);
        Mockito.verify(searchRepository, Mockito.times(1)).getCategoryStores(command, SearchService.DEFAULT_RADIUS_METER);
    }


}
