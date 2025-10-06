package com.example.demo.repository;

import com.example.demo.model.entity.Order;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    // 🔹 自訂重置自增 ID 方法
    @Modifying
    @Query(value = "ALTER TABLE `order` AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}
