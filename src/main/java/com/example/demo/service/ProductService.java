package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.model.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 取得所有商品 (返回 DTO)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 取得單一商品 (返回 DTO)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO);
    }
    // 取得 Product 本體 (給 CartService 使用)
    public Optional<Product> getProductEntityById(Long id) {
        return productRepository.findById(id);
    }


    // 創建商品 (返回 DTO)
    public ProductDTO createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    // 更新商品 (返回 DTO)
    public ProductDTO updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setColor(updatedProduct.getColor());
                    existingProduct.setCategory(updatedProduct.getCategory());
                    existingProduct.setSizes(updatedProduct.getSizes());
                    existingProduct.setImageUrls(updatedProduct.getImageUrls());

                    Product savedProduct = productRepository.save(existingProduct);
                    return convertToDTO(savedProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // 刪除商品
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // **將 Product 轉換為 ProductDTO**
    private ProductDTO convertToDTO(Product product) {
        List<String> availableSizes = product.getSizes().stream()
                .map(size -> size.getSize())
                .collect(Collectors.toList());

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getColor(),
                product.getImageUrls(),
                product.getCategory(),
                availableSizes
        );
    }
}
