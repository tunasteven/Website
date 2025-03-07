package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.model.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // 新增商品（只有管理員可以訪問）
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(convertToEntity(productDTO));
        return ResponseEntity.ok(convertToDTO(createdProduct));
    }

    // 更新商品（只有管理員可以訪問）
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, convertToEntity(productDTO));
        return ResponseEntity.ok(convertToDTO(updatedProduct));
    }

    // 刪除商品（只有管理員可以訪問）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    // **DTO 轉換方法**
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setColor(dto.getColor());
        product.setImageUrls(dto.getImageUrls());
        product.setCategory(dto.getCategory());
        return product;
    }

    private ProductDTO convertToDTO(ProductDTO product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getColor(),
                product.getImageUrls(),
                product.getCategory(),
                null // 這裡假設你不需要回傳 sizes
        );
    }
}
