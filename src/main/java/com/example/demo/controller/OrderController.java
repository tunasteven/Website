package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderRequest;
import com.example.demo.model.entity.User;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // ✅ 確認訂單
    @PostMapping("/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@RequestBody OrderRequest orderRequest) {
        if (orderRequest.getShippingAddress() == null || orderRequest.getPaymentMethod() == null) {
            return ResponseEntity.badRequest().build();
        }

        // ✅ 取得當前使用者
        User currentUser = getCurrentUser();

        // ✅ 直接轉換購物車為 `OrderDTO`（避免先回傳 `Order` 再轉換）
        OrderDTO orderDTO = cartService.convertCartToOrder(currentUser, orderRequest.getShippingAddress(), orderRequest.getPaymentMethod());

        return ResponseEntity.ok(orderDTO);
    }

    // ✅ 重置訂單和訂單項目 ID
    @PostMapping("/reset")
    public ResponseEntity<Void> resetOrderIds() {
        orderService.resetOrderIds();
        return ResponseEntity.ok().build();
    }

    // ✅ 獲取當前登入的使用者
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return userService.findByEmail(((UserDetails) principal).getUsername());
        } else {
            throw new IllegalStateException("未驗證的用戶");
        }
    }
}
