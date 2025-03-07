package com.example.demo.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;    // 街道
    private String city;      // 城市
    private String state;     // 省/州
    private String zipCode;   // 郵遞區號
    private String country;   // 國家
}
