package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.dto.UserRegistrationRequest; // 確保這個 DTO 類別是正確的
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AdminController {

    @Autowired
    private UserService userService;

    // 取得所有使用者
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 根據ID取得特定使用者
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    // 新增使用者
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRegistrationRequest registrationRequest) {
        // 使用 UserService 的 registerNewUser 方法
        User newUser = userService.registerNewUser(registrationRequest);
        return ResponseEntity.ok(newUser);
    }

    // 更新使用者資料
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 根據需求更新字段
        user.setEmail(updatedUser.getEmail());

        // 不直接從請求中更新密碼，需考慮密碼安全性
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(updatedUser.getPassword());
        }

        user.setPhone(updatedUser.getPhone());
        user.setCity(updatedUser.getCity());
        user.setAddress(updatedUser.getAddress());
        user.setEnabled(updatedUser.isEnabled());
        // 更新更多字段...

        User savedUser = userService.updateUser(user);
        return ResponseEntity.ok(savedUser);
    }

    // 刪除使用者
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
