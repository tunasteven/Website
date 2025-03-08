package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long cartItemId; // 購物車項目的 ID
    private Long productId;  // 產品的 ID
    private String productName; // 只傳遞需要的商品信息
    private int quantity;
    private BigDecimal price;
    private String size; // 只回傳購物車中選擇的尺寸
    private BigDecimal totalPrice; // 用於存儲該項目的總價
    private String imageUrl; // 新增圖片欄位
}
