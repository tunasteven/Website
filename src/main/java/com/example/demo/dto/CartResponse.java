package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // ✅ 確保可以使用無參數建構子
@AllArgsConstructor // ✅ 讓 `new CartResponse(cart.getId(), items)` 可以使用
public class CartResponse {
    private Long cartId;  // 購物車 ID
    private List<CartItemResponse> items;  // 購物車中的所有項目
}

