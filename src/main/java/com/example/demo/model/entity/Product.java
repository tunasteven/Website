package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(length = 2000) // 假設我們將長度設為2000，可以根據需要調整
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String color;

    @ElementCollection
    private List<String> imageUrls;

    private String category; // 用來區分商品類型（例如 "Men's Tops", "Bottoms", "Outerwear"）

    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private List<Size> sizes;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Size {
        private String size;
        private int quantity;
    }
}
