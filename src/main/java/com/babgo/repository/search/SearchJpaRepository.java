package com.babgo.repository.search;

import com.babgo.domain.search.Search;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SearchJpaRepository extends CrudRepository<Search, UUID> {


    @Query(value = """
        SELECT *
        FROM p_store_searches s
        WHERE ST_DWithin(
                geography(ST_SetSRID(ST_Point(s.longitude, s.latitude), 4326)),
                geography(ST_SetSRID(ST_Point(:longitude, :latitude), 4326)),
                :radiusMeters
              )
        AND s.store_name ILIKE CONCAT(:keyword, '%')
        ORDER BY
          CASE WHEN :sort = 'DISTANCE' THEN ST_Distance(
              geography(ST_SetSRID(ST_Point(s.longitude, s.latitude), 4326)),
              geography(ST_SetSRID(ST_Point(:longitude, :latitude), 4326))
          ) END ASC,
          CASE WHEN :sort = 'RATING' THEN s.avg_rating END DESC,
          CASE WHEN :sort = 'LIKES' THEN s.likes END DESC,
          CASE WHEN :sort = 'CREATED' THEN s.created_at END DESC
        LIMIT :size OFFSET (:page * :size)
        """, nativeQuery = true)
    List<Search> getStoreSearch(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("keyword") String keyword,
        @Param("sort") String sort,
        @Param("page") int page,
        @Param("size") int size,
        @Param("radiusMeters") double radiusMeters
    );


    @Query(value = """
        SELECT *
        FROM p_store_searches s
        WHERE ST_DWithin(
                geography(ST_SetSRID(ST_Point(s.longitude, s.latitude), 4326)),
                geography(ST_SetSRID(ST_Point(:longitude, :latitude), 4326)),
                :radiusMeters
              )
        AND s.category_id = CAST(:categoryId AS uuid)
        ORDER BY
          CASE WHEN :sort = 'DISTANCE' THEN ST_Distance(
              geography(ST_SetSRID(ST_Point(s.longitude, s.latitude), 4326)),
              geography(ST_SetSRID(ST_Point(:longitude, :latitude), 4326))
          ) END ASC,
          CASE WHEN :sort = 'RATING' THEN s.avg_rating END DESC,
          CASE WHEN :sort = 'LIKES' THEN s.likes END DESC,
         CASE WHEN :sort = 'CREATED' THEN s.created_at END DESC
        LIMIT :size OFFSET (:page * :size)
        """, nativeQuery = true)
    List<Search> getCategorySearch(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("categoryId") String categoryId,
        @Param("sort") String sort,
        @Param("page") int page,
        @Param("size") int size,
        @Param("radiusMeters") double radiusMeters
    );
}
