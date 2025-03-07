package com.example.demo.repository;

import com.example.demo.model.entity.Cart;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 根據用戶查找購物車
    Cart findByUser(User user);
}
