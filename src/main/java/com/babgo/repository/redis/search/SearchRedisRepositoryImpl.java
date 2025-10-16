package com.babgo.repository.redis.search;

import static com.babgo.repository.redis.search.SearchRedisKeyFactory.getCategoryRegionSortCache;

import com.babgo.domain.search.SearchCommand.Create;
import com.babgo.domain.search.SearchCommand.CreateResult;
import com.babgo.domain.search.SearchRedisRepository;
import com.babgo.domain.search.SearchSort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchRedisRepositoryImpl implements SearchRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public SearchRedisRepositoryImpl(@Qualifier("searchRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }




    @Override
    public List<CreateResult> getCategoryRegionCache(Create searchCommand) {

        // 키 가져오기
        String key = getCategoryRegionSortCache(searchCommand.getRegionCode(), searchCommand.getKeyword(), searchCommand.getSort());

        int page = Math.max(1, searchCommand.getPage());
        int size = Math.max(1, searchCommand.getSize());
        int start = (page - 1) * size;
        int end = start + size - 1;

        // score 내림차순으로 해당 구간 조회
        Set<String> jsonSet;
        if (SearchSort.DISTANCE.name().equals(searchCommand.getSort())) {
            jsonSet = redisTemplate.opsForZSet().range(key, start, end); // 오름차순
        } else {
            jsonSet = redisTemplate.opsForZSet().reverseRange(key, start, end); // 내림차순
        }

        if (jsonSet == null) {
            return List.of();
        }


        ObjectMapper objectMapper = new ObjectMapper();


        List<CreateResult> results = jsonSet.stream()
            .map(json -> {
                try {
                    return objectMapper.readValue(json, CreateResult.class);
                } catch (JsonProcessingException e) {
                    log.error("redis 역직렬화 실패: {}", e.getMessage(), e);
                    throw new RuntimeException();
                }
            })
            .toList();

        log.info("redis 캐시 조회 성공 key: {}, page: {}, size: {}, 결과 수: {}", key, page, size, results.size());

        return results;
    }
}
