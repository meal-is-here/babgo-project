package com.babgo.domain.favorite;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.user.User;
import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "p_favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "menu_id"})
})
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "favorite_id")
    private UUID favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    private String option;
    private int quantity;

    private Favorite(User user, Menu menu, String option, int quantity) {
        this.user = user;
        this.menu = menu;
        this.option = option;
        this.quantity = quantity;
    }

    public static Favorite create(User user, Menu menu, String option, int quantity) {
        return new Favorite(user, menu, option, quantity);
    }
}
