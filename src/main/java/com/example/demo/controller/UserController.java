package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 獲取當前用戶信息
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();  // 從 SecurityContext 中取得 email

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return ResponseEntity.ok(user);
    }

    // 更新用戶信息
    @PatchMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 只更新請求中提供的字段
        if (updatedUser.getAddress() != null) {
            user.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getBirthdate() != null) {
            user.setBirthdate(updatedUser.getBirthdate());
        }
        if (updatedUser.getTitle() != null) {
            user.setTitle(updatedUser.getTitle());
        }
        if (updatedUser.getCity() != null) {
            user.setCity(updatedUser.getCity());
        }
        // 可根據需要更新更多字段

        User savedUser = userService.updateUser(user);
        return ResponseEntity.ok(savedUser);
    }
}
