package com.babgo.domain.like;

import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // register like
    public Like registerLike(Long userId, UUID storeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (likeRepository.existsByUserUserIdAndStoreStoreId(userId, storeId)) {
            throw new CustomException(ErrorCode.LIKE_ALREADY_EXIST);
        }

        Like like = Like.of(user, store);
        return likeRepository.save(like);
    }
}
