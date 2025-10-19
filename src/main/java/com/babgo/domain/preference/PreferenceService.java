package com.babgo.domain.preference;

import com.babgo.controller.preference.dto.FavoriteInfo;
import com.babgo.controller.preference.dto.LikeInfo;
import com.babgo.controller.preference.dto.PreferenceResponse;
import com.babgo.domain.favorite.Favorite;
import com.babgo.domain.favorite.FavoriteRepository;
import com.babgo.domain.like.Like;
import com.babgo.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;

    public PreferenceResponse getUserPreferences(Long userId) {
        List<Like> likes = likeRepository.findAllByUserUserIdOrderByCreatedAtDesc(userId);
        List<Favorite> favorites = favoriteRepository.findAllByUserUserIdOrderByCreatedAtDesc(userId);

        List<LikeInfo> likeInfos = likes.stream()
                .map(like -> new LikeInfo(
                        like.getStore().getStoreId(),
                        like.getStore().getStoreName()
                ))
                .collect(Collectors.toList());

        List<FavoriteInfo> favoriteInfos = favorites.stream()
                .map(fav -> new FavoriteInfo(
                        fav.getMenu().getMenuId(),
                        fav.getMenu().getName(),
                        fav.getQuantity()
                ))
                .collect(Collectors.toList());

        return new PreferenceResponse(likeInfos, favoriteInfos);
    }
}
