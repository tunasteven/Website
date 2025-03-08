package com.example.demo.model.entity;

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
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    private Product product;

    private int quantity;

    private BigDecimal price = BigDecimal.ZERO;

    private String size;
    private String imageUrl; // ✅ 新增圖片欄位

    public BigDecimal getTotalPrice() {
        System.out.println("Calculating total price for CartItem ID: " + this.id + ", Quantity: " + quantity);
        if (price == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public OrderItem convertToOrderItem() {
        System.out.println("Converting CartItem ID: " + this.id + " with size: " + this.size); // 調試日誌
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(this.product);
        orderItem.setQuantity(this.quantity);
        orderItem.setPrice(this.price != null ? this.price : BigDecimal.ZERO);
        orderItem.setSize(this.size);
        return orderItem;
    }
}
