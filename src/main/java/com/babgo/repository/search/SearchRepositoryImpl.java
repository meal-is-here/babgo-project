package com.babgo.repository.search;

import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final SearchJpaRepository searchJpaRepository;

    @Override
    public List<Search> getStoreSearch(SearchCommand.Create searchCommand, double radiusMeters) {
        return searchJpaRepository.getStoreSearch(
            searchCommand.getLatitude(),
            searchCommand.getLongitude(),
            searchCommand.getKeyword(),
            searchCommand.getSort(),
            searchCommand.getPage(),
            searchCommand.getSize(),
            radiusMeters
        );
    }

    @Override
    public List<Search> getCategorySearch(SearchCommand.Create searchCommand, double radiusMeters) {
        return searchJpaRepository.getCategorySearch(
            searchCommand.getLatitude(),
            searchCommand.getLongitude(),
            searchCommand.getKeyword(),
            searchCommand.getSort(),
            searchCommand.getPage(),
            searchCommand.getSize(),
            radiusMeters
        );
    }
}
