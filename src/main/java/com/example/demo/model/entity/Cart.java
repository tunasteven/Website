package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnore // 忽略在 JSON 中的 user 屬性
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    private String size;

    // 計算購物車總價
    public BigDecimal getTotalPrice() {
        System.out.println("Calculating total price for Cart ID: " + this.id);
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Order convertToOrder(Address shippingAddress, PaymentMethod paymentMethod) {
        Order order = new Order();
        order.setUser(this.user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setOrderItems(this.items.stream()
                .map(CartItem::convertToOrderItem)
                .collect(Collectors.toList()));
        order.setTotalPrice(this.getTotalPrice());
        order.setSize(this.size);
        return order;
    }
}
