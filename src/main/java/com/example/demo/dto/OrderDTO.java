package com.example.demo.dto;

import com.example.demo.model.entity.Address;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private Long orderId;
    private Address shippingAddress;
    private String paymentMethod;
    private List<OrderItemDTO> orderItems;
}
