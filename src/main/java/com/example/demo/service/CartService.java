package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.CartNotFoundException;
import com.example.demo.model.entity.*;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final JdbcTemplate jdbcTemplate;

    private final CartRepository cartRepository;

    private final OrderRepository orderRepository;

    public CartService(JdbcTemplate jdbcTemplate, CartRepository cartRepository, OrderRepository orderRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        // 用於查詢商品
    }

    // 根據用戶獲取購物車
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    @Transactional
    public void clearAndResetCart() {
        try {
            // 關閉外鍵檢查，避免外鍵約束導致的錯誤（包括與 User 的外鍵）
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

            // 清空 cart_item 表
            jdbcTemplate.execute("TRUNCATE TABLE cart_item;");

            // 清空 cart 表
            jdbcTemplate.execute("TRUNCATE TABLE cart;");

            // 重置 AUTO_INCREMENT
            jdbcTemplate.execute("ALTER TABLE cart AUTO_INCREMENT = 1;");
            jdbcTemplate.execute("ALTER TABLE cart_item AUTO_INCREMENT = 1;");

            // 恢復外鍵檢查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

        } catch (Exception e) {
            throw new RuntimeException("Failed to reset cart and cart_item tables", e);
        }
    }



    // 將商品添加到購物車
    @Autowired
    private ProductService productService;  // 新增 ProductService 來獲取 Product 物件

    public Cart addProductToCart(User user, ProductDTO productDTO, int quantity, String size) {
        // 查找該用戶的購物車
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
        }

        // **這裡把 DTO 轉回 Product**
        Product product = productService.getProductEntityById(productDTO.getId())
                .orElseThrow(() -> new RuntimeException("找不到商品 ID: " + productDTO.getId()));

        // 查找購物車中是否已存在相同產品和尺寸的項目
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 如果該產品和尺寸已存在，則更新數量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // 如果不存在，則新增購物車項目
            CartItem newItem = new CartItem();
            newItem.setProduct(product); // 這裡要確保是 `Product` 物件
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setCart(cart);
            newItem.setSize(size);
            cart.getItems().add(newItem);
        }

        // 保存並返回更新後的購物車
        return cartRepository.save(cart);
    }




    // 更新購物車中的商品數量
    public Cart updateCartItem(User user, Long cartItemId, int quantity) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            throw new CartNotFoundException("購物車未找到");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("購物車項目未找到"));

        cartItem.setQuantity(quantity); // 更新數量
        return cartRepository.save(cart); // 儲存購物車
    }

    // 刪除購物車中的商品
    public void removeCartItem(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            throw new CartNotFoundException("購物車未找到");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("購物車項目未找到"));

        cart.getItems().remove(cartItem); // 從購物車中移除該項目
        cartRepository.save(cart); // 儲存更新後的購物車
    }

    // 將購物車轉換為訂單
    public Order convertCartToOrder(User user, Address shippingAddress, PaymentMethod paymentMethod) {
        // 根據當前用戶取得購物車
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new CartNotFoundException("購物車是空的或未找到");
        }

        // 創建新訂單
        Order order = new Order();
        order.setUser(user);  // 設置訂單對應的用戶
        order.setShippingAddress(shippingAddress);  // 設置運送地址
        order.setPaymentMethod(paymentMethod);  // 設置付款方式

        // 將購物車中的每個 CartItem 轉換為 OrderItem
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setSize(cartItem.getSize());
                    orderItem.setOrder(order); // 設置關聯的訂單
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);  // 設置訂單項目
        order.setTotalPrice(cart.getTotalPrice());  // 設置總價

        // 儲存訂單到資料庫
        return orderRepository.save(order);
    }
}
