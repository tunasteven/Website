package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String color;
    private List<String> imageUrls;
    private String category;
    @JsonProperty("sizes")
    private List<SizeDTO> sizes; // 改為包含 size 和 quantity 的 SizeDTO
}

