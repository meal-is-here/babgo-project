package com.babgo.repository.search;

import com.babgo.domain.search.QSearch;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {


    private final JPAQueryFactory jpaQueryFactory;

    private final SearchJpaRepository searchJpaRepository;


    @Override
    public List<Search> getStoreSearch(SearchCommand.Create searchCommand, double radiusMeters) {

        QSearch qSearch = QSearch.search;

        // WHERE 조건을 만들기 위한 빌더
        BooleanBuilder builder = new BooleanBuilder();
        String keyword = searchCommand.getKeyword();
        String regionCode = searchCommand.getRegionCode();

        // 키워드 조건
        if (keyword != null && !keyword.isEmpty()) {
            builder.or(qSearch.storeName.like("%" + keyword + "%"));
        }

        // 지역 조건
        if (regionCode != null && !regionCode.isEmpty()) {
            builder.and(qSearch.regionCode.eq(regionCode));
        }

        // 정렬 조건 생성
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(searchCommand.getLongitude(),
            searchCommand.getLatitude(), searchCommand.getSort(), qSearch);

        return jpaQueryFactory
            .selectFrom(qSearch)
            .where(builder)
            .orderBy(orderSpecifier)
            .offset((long) (searchCommand.getPage() - 1) * searchCommand.getSize())
            .limit(searchCommand.getSize())
            .fetch();
    }

    @Override
    public List<Search> getCategorySearch(SearchCommand.Create searchCommand, double radiusMeters) {

        QSearch qSearch = QSearch.search;

        // WHERE 조건을 만들기 위한 빌더
        BooleanBuilder builder = new BooleanBuilder();
        UUID keyword;
        String regionCode = searchCommand.getRegionCode();

        // 키워드 조건
        if (searchCommand.getKeyword() != null && !searchCommand.getKeyword().isEmpty()) {
            try {
                keyword = UUID.fromString(searchCommand.getKeyword());
                builder.and(qSearch.categoryId.eq(keyword));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "keyword가 UUID 형식이 아닙니다: " + searchCommand.getKeyword());

            }
        }

        // 지역 조건
        if (regionCode != null && !regionCode.isEmpty()) {
            builder.and(qSearch.regionCode.eq(regionCode));
        }

        // 정렬 조건 생성
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(searchCommand.getLongitude(),
            searchCommand.getLatitude(), searchCommand.getSort(), qSearch);

        return jpaQueryFactory
            .selectFrom(qSearch)
            .where(builder)
            .orderBy(orderSpecifier)
            .offset((long) (searchCommand.getPage() - 1) * searchCommand.getSize())
            .limit(searchCommand.getSize())
            .fetch();
    }


    private OrderSpecifier<?> getOrderSpecifier(double longitude, double latitude, String sort,
        QSearch search) {

        // 거리 계산
        NumberExpression<Double> distanceExpr = Expressions.numberTemplate(
            Double.class,
            "ST_Distance(" +
                "geography(ST_SetSRID(ST_Point({0}, {1}), 4326)), " +
                "geography(ST_SetSRID(ST_Point({2}, {3}), 4326)))",
            search.longitude,
            search.latitude,
            longitude,
            latitude
        );

        return switch (sort.toUpperCase()) {
            case "DISTANCE" -> distanceExpr.asc();
            case "RATING" -> search.avgRating.desc();
            case "LIKES" -> search.likes.desc();
            case "ORDER_COUNT" -> search.orderCount.desc();
            default -> search.createdAt.desc(); // 기본값

        };
    }


    @Override
    public void saveSearch(Search search) {
        searchJpaRepository.save(search);
    }

    @Override
    public Search findByStoreId(UUID storeId) {
        return searchJpaRepository.findByStoreId(storeId);
    }

}
