package com.kt.hiorder_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "menu_category")
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_category_id")
    private Long menuCategoryId;

    @Column(name = "menu_category_name", length = 40, nullable = false)
    private String menuCategoryName;

    @Column(name = "menu_category_name_en", length = 40)
    private String menuCategoryNameEn;

    @Column(name = "menu_category_name_zh", length = 40)
    private String menuCategoryNameZh;

    @Column(name = "menu_category_name_jp", length = 40)
    private String menuCategoryNameJp;


    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
