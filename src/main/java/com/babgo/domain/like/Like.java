package com.babgo.domain.like;

import com.babgo.domain.store.Store;
import com.babgo.domain.user.User;
import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "store_id"})
})
public class Like extends BaseTimeEntity {

    @Id
    private UUID likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private Like(User user, Store store) {
        this.user = user;
        this.store = store;
    }

    public static Like of(User user, Store store) {
        return new Like(user, store);
    }
}
