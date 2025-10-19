package com.babgo.domain.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.babgo.MockTest;
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

    @Mock
    private SearchRedisRepository searchRedisRepository;

    private double latitude = 37.5665;       // 서울 위도
    private double longitude = 126.9780;      // 서울 경도
    private SearchType searchType; // 검색 타입 (예: 가게)
    private String regionCode = "11110"; // 지역코드
    private String keyword = "치킨";     // 검색 키워드
    private String sort = "DISTANCE";    // 정렬 기준 (거리순)
    private int page = 0;
    private int size = 10;


    @Test
    void 레디스캐시없을때_카테고리_타입으로_가게_조회_성공(){

        // given: 조회 요청에 사용할 Command 파라미터 준비
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), keyword, sort, page, size);

        // given: Repository가 반환할 가짜 Search 객체(Mock) 준비
        Search search1 = Mockito.mock(Search.class);
        Search search2 = Mockito.mock(Search.class);

        List<Search> expected = List.of(search1, search2);


        Mockito.when(searchRepository.getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(expected);
        Mockito.when(searchRedisRepository.getCategoryRegionCache(command)).thenReturn(List.of());

        // when: 조회
        List<SearchCommand.CreateResult> result = searchService.getCategorySearch(command);

        // then: 결과와 호출 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        verify(searchRepository, Mockito.times(1)).getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER);
        Mockito.verifyNoMoreInteractions(searchRepository);

    }

    @Test
    void 레디스캐시없을때_카테고리_타입으로_가게_조회_결과_없으면_빈값_반환(){

        // given: 조회 요청에 사용할 Command 파라미터 준비 및 조회 시 빈값 세팅
        SearchCommand.Create command = SearchCommand.Create.of(latitude, longitude, regionCode, SearchType.KATEGORIE.name(), keyword, sort, page, size);
        List<Search> emptyResult = Collections.emptyList();

        Mockito.when(searchRepository.getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(emptyResult);

        Mockito.when(searchRedisRepository.getCategoryRegionCache(command)).thenReturn(List.of());


        // when: 조회
        List<SearchCommand.CreateResult> result = searchService.getCategorySearch(command);

        // then: 빈 결과 반환 확인
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
        verify(searchRepository, Mockito.times(1)).getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER);

    }


    @Test
    void 레디스캐시없을때_카테고리_타입으로_가게_조회_성공_시_반환_값_10개(){

        // given: 조회 요청에 사용할 Command 파라미터 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, regionCode, SearchType.KATEGORIE.name(), keyword, sort, page, size
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
                1,                                      // ordercount
                "영업중",                                // storeStatus
                37.5665 + i * 0.001,                    // latitude
                126.9780 + i * 0.001                    // longitude
            ))
            .collect(Collectors.toList());


        Mockito.when(searchRepository.getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER))
            .thenReturn(mockResult);

        Mockito.when(searchRedisRepository.getCategoryRegionCache(command)).thenReturn(List.of());

        // when: 조회
        List<SearchCommand.CreateResult> result = searchService.getCategorySearch(command);

        // then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());
        verify(searchRepository, Mockito.times(1))
            .getCategorySearch(command, SearchService.DEFAULT_RADIUS_METER);

    }

    @Test
    void 가게명_검색타입으로_가게_조회_성공() {
        // given: 카테고리 검색 요청 반환할 가짜 Search 객체(Mock) 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, regionCode, SearchType.STORE.name(), keyword, sort, page, size
        );

        Search search1 = Mockito.mock(Search.class);
        Search search2 = Mockito.mock(Search.class);

        List<Search> expected = List.of(search1, search2);

        Mockito.when(searchRepository.getStoreSearch(command, SearchService.DEFAULT_RADIUS_METER)).thenReturn(expected);

        // when: 카테고리 검색 실행
        List<SearchCommand.CreateResult> result = searchService.getStoreSearch(command);

        // then: 결과 검증
        Assertions.assertNotNull(result);
        verify(searchRepository, Mockito.times(1)).getStoreSearch(command, SearchService.DEFAULT_RADIUS_METER);
    }


    @Test
    void 카테고리_검색타입_조회시_레디스에_데이터_있음(){

        // given: 카테고리 검색 요청 반환할 가짜 Search 객체(Mock) 준비
        SearchCommand.Create command = SearchCommand.Create.of(
            latitude, longitude, regionCode, SearchType.STORE.name(), keyword, sort, page, size
        );

        SearchCommand.CreateResult searchResult1 = Mockito.mock(SearchCommand.CreateResult.class);
        SearchCommand.CreateResult searchResult2 = Mockito.mock(SearchCommand.CreateResult.class);

        List<SearchCommand.CreateResult> expected = List.of(searchResult1, searchResult2);

        Mockito.when(searchRedisRepository.getCategoryRegionCache(command)).thenReturn(expected);


        // whrn
        List<SearchCommand.CreateResult> result = searchService.getCategorySearch(command);

        // then: 결과 검증 db호출 안함
        Assertions.assertNotNull(result);
        verify(searchRepository, never()).getCategorySearch(any(), anyDouble());


    }



}
