package com.babgo.domain.menu;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_menus")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @Column(name = "menu_id")
    private UUID menuId;

    @NotNull
    private String name;

    @NotNull
    private Long price;

    private String description;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_status")
    private MenuStatus menuStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "store_id")
    private UUID storeId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE) 만약 부모에서 OneToMany로 케스케이드 설정시 없애는것 권장
//    private Store store;  // FK 매핑

//    JPA수준에서 관리하려면 아래 코드를 Store엔티티에 추가. 그리하면 Store엔티티가 JoinColumn가 붙어있는
//    store 필드를 인식하고(mappedBy = "store") 자식으로 인식하게 된다.
//    @OneToMany(
//        mappedBy = "store",
//        cascade = CascadeType.ALL,
//        orphanRemoval = true
//    )
//    private List<Menu> menus = new ArrayList<>();

    public Menu(String name, Long price, String description, String category,
                MenuStatus menuStatus, String createdBy, UUID storeId) {
//                                                      Store store
        this.menuId = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.menuStatus = menuStatus;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.storeId = storeId;
//        this.store = store;
    }

    // 메뉴 상태 변경을 위한 세터 메서드 (세터 최소화를 위함)
    public void changeStatus(MenuStatus menuStatus, String updatedBy) {
        this.menuStatus = menuStatus;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    // 메뉴 정보 변경을 위한 세터 메서드
    public void updateMenuInfo(String name, Long price, String description,
                               String category, String updatedBy) {
        // non-nullable 값에 null 값이 입력 되면 변경이 되지 아니함
        if (name != null) this.name = name;
        if (price != null) this.price = price;
        this.description = description;
        this.category = category;

        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    // 메뉴 논리 삭제를 위한 세터 메서드
    // 실제 DB row는 그대로 남음 -> 삭제된 메뉴는 조회할 때 상태로 필터링 가능
    public void deleteMenu(String deletedBy) {
        this.menuStatus = MenuStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;

        // 업데이트 시각/사용자도 같이 갱신
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = deletedBy;
    }
}
