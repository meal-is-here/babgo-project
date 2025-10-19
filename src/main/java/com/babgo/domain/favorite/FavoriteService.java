package com.babgo.domain.favorite;

import com.babgo.controller.favorite.dto.FavoriteRequest;
import com.babgo.controller.favorite.dto.FavoriteResponse;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import com.babgo.domain.menu.MenuStatus;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    // add favorite
    @Transactional
    public FavoriteResponse addFavorite(Long userId, FavoriteRequest request) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        if (menu.getMenuStatus() == MenuStatus.SOLD_OUT || menu.getMenuStatus() == MenuStatus.DELETED) {
            throw new CustomException(ErrorCode.MENU_UNAVAILABLE);
        }

        favoriteRepository.findByUserUserIdAndMenuMenuId(userId, menu.getMenuId())
                .ifPresent(f -> { throw new CustomException(ErrorCode.FAVORITE_ALREADY_EXISTS); });

        Favorite favorite = Favorite.create(user, menu, request.getOption(), request.getQuantity());
        Favorite saved = favoriteRepository.save(favorite);

        return FavoriteResponse.from(saved);
    }
}
