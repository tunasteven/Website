package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private Long cartId;  // 購物車 ID
    private List<CartItemResponse> items;  // 購物車中的所有項目
}

