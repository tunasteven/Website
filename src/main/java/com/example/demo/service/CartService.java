package com.example.demo.service;

import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.CartNotFoundException;
import com.example.demo.model.entity.*;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final JdbcTemplate jdbcTemplate;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService; // ✅ 新增 OrderService


    @Autowired
    private ProductService productService;



    public CartService(JdbcTemplate jdbcTemplate, CartRepository cartRepository, OrderRepository orderRepository, OrderService orderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService; // ✅ 注入 OrderService
    }

    // ✅ 取得使用者的購物車，並回傳 `CartResponse`
    public CartResponse getCartByUser(User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            return new CartResponse(0L, new ArrayList<>()); // 避免 null 錯誤
        }
        return convertToCartResponse(cart);
    }

    // ✅ 新增商品到購物車
    @Transactional
    public CartResponse addProductToCart(User user, ProductDTO productDTO, int quantity, String size) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>()); // 確保 items 不為 null
        }

        Product product = productService.getProductEntityById(productDTO.getId())
                .orElseThrow(() -> new RuntimeException("找不到商品 ID: " + productDTO.getId()));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setCart(cart);
            newItem.setSize(size);
            newItem.setImageUrl(product.getImageUrls() != null && !product.getImageUrls().isEmpty()
                    ? product.getImageUrls().get(0)
                    : "https://via.placeholder.com/150");

            cart.getItems().add(newItem);
        }

        Cart updatedCart = cartRepository.save(cart);
        return convertToCartResponse(updatedCart);
    }

    // ✅ 更新購物車內商品數量
    @Transactional
    public CartResponse updateCartItem(User user, Long cartItemId, int quantity) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            throw new CartNotFoundException("購物車未找到");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("購物車項目未找到"));

        cartItem.setQuantity(quantity);
        Cart updatedCart = cartRepository.save(cart);
        return convertToCartResponse(updatedCart);
    }

    // ✅ 將 `Cart` 轉換為 `CartResponse`
    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSize(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getImageUrl() != null ? item.getImageUrl() : "https://via.placeholder.com/150"
                ))
                .collect(Collectors.toList());

        return new CartResponse(cart.getId(), items);
    }
    @Transactional
    public OrderDTO convertCartToOrder(User user, Address shippingAddress, PaymentMethod paymentMethod) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new CartNotFoundException("購物車是空的或未找到");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setSize(cartItem.getSize());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order.setTotalPrice(cart.getTotalPrice());

        Order savedOrder = orderRepository.save(order);

        // ✅ 轉換 `Order` 為 `OrderDTO`，直接回傳
        return orderService.convertToDTO(savedOrder);
    }
    @Transactional
    public void clearCart(User user) {
        // 清空該使用者的購物車邏輯
        cartRepository.deleteByUser(user);
    }


    // ✅ 刪除購物車中的商品
    @Transactional
    public void removeCartItem(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            throw new CartNotFoundException("購物車未找到");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("購物車項目未找到"));

        cart.getItems().remove(cartItem);
        cartRepository.save(cart); // ✅ 儲存更新後的購物車
    }

    // ✅ 清空購物車並重置 ID
    @Transactional
    public void clearAndResetCart() {
        try {
            // 關閉外鍵檢查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

            // 清空 `cart_item` 和 `cart` 資料表
            jdbcTemplate.execute("TRUNCATE TABLE cart_item;");
            jdbcTemplate.execute("TRUNCATE TABLE cart;");

            // 重置 `AUTO_INCREMENT`
            jdbcTemplate.execute("ALTER TABLE cart AUTO_INCREMENT = 1;");
            jdbcTemplate.execute("ALTER TABLE cart_item AUTO_INCREMENT = 1;");

            // 恢復外鍵檢查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

        } catch (Exception e) {
            throw new RuntimeException("清空購物車失敗", e);
        }

    }



}
