package com.example.demo.model.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order; // 設定這個 OrderItem 屬於哪個訂單

    @ManyToOne
    private Product product; // 訂單項目對應的產品

    private int quantity; // 購買數量

    private BigDecimal price; // 當前價格（可能與購物車中的價格有所不同）

    private String size; // 新增的 size 欄位

    // 計算該項目的總價
    public BigDecimal getTotalPrice() {
        if (price == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
