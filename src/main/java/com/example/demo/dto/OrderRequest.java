package com.example.demo.dto;

import com.example.demo.model.entity.Address;
import com.example.demo.model.entity.PaymentMethod;
import lombok.Data;

@Data
public class OrderRequest {
    private Address shippingAddress;    // 配送地址
    private PaymentMethod paymentMethod; // 付款方式
}
