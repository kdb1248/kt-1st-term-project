package com.kt.hiorder_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "menu_description", length = 200)
    private String menuDescription;

    // 다국어 컬럼 (영어)
    @Column(name = "menu_name_en", length = 100)
    private String menuNameEn;

    @Column(name = "menu_description_en", length = 200)
    private String menuDescriptionEn;

    // 다국어 컬럼 (중국어)
    @Column(name = "menu_name_zh", length = 100)
    private String menuNameZh;

    @Column(name = "menu_description_zh", length = 200)
    private String menuDescriptionZh;

    // 다국어 컬럼 (일본어)
    @Column(name = "menu_name_jp", length = 100)
    private String menuNameJp;

    @Column(name = "menu_description_jp", length = 200)
    private String menuDescriptionJp;

    @Column(name = "price", nullable = false)
    private BigDecimal price;  // numeric(10,0) → long or BigDecimal

    @Column(name = "menu_image_url", columnDefinition = "text")
    private String menuImageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "menu_category_id", nullable = false)
    private MenuCategory menuCategory;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
