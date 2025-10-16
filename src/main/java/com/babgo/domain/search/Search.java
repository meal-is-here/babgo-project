package com.babgo.domain.search;

import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "p_store_searches")
public class Search extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="search_id")
    private UUID searchId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "avg_rating")
    private double avgRating;

    @Column(name = "likes")
    private int likes;

    @Column(name = "order_count")
    private int orderCount;

    @Column(name = "store_status")
    private String storeStatus;


    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;




    private Search(UUID storeId, String regionCode, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, int orderCount, String storeStatus, double latitude, double longitude) {
        this.storeId = storeId;
        this.regionCode = regionCode;
        this.storeName = storeName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.avgRating = avgRating;
        this.likes = likes;
        this.orderCount = orderCount;
        this.storeStatus = storeStatus;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public static Search of(UUID storeId, String regionCode, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, int orderCount, String storeStatus, double latitude, double longitude){
        return new Search(storeId, regionCode, storeName, categoryId, categoryName, avgRating, likes, orderCount, storeStatus, latitude, longitude);
    }


}
