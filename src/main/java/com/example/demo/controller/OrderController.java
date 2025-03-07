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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 確認訂單
    @PostMapping("/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@RequestBody OrderRequest orderRequest) {
        // 檢查訂單請求的有效性
        if (orderRequest.getShippingAddress() == null || orderRequest.getPaymentMethod() == null) {
            return ResponseEntity.badRequest().build();
        }

        // 取得當前已登入的用戶
        User currentUser = getCurrentUser();

        // 將購物車轉換成訂單並保存
        var newOrder = cartService.convertCartToOrder(currentUser, orderRequest.getShippingAddress(), orderRequest.getPaymentMethod());
        orderService.saveOrder(newOrder);

        // 將訂單轉換為 DTO 並返回
        OrderDTO orderDTO = orderService.convertToDTO(newOrder);
        return ResponseEntity.ok(orderDTO);
    }

    // 重置訂單和訂單項目 ID
    @PostMapping("/reset")
    public ResponseEntity<Void> resetOrderIds() {
        orderService.resetOrderIds();
        return ResponseEntity.ok().build();
    }

    // 獲取當前用戶的工具方法
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
        return userService.findByEmail(username);
    }
}
