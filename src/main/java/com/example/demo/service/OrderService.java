package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.model.entity.Order;
import com.example.demo.model.entity.OrderItem;
import com.example.demo.model.entity.User;
import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final JdbcTemplate jdbcTemplate;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(JdbcTemplate jdbcTemplate, OrderRepository orderRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderRepository = orderRepository;
    }
    public List<OrderDTO> getOrdersForUser(User user) {
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 重設 order 和 order_item 表的 ID
    @Transactional
    public void resetOrderIds() {
        try {
            // 暫時關閉外鍵檢查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

            // 清空 order_item 表
            jdbcTemplate.execute("TRUNCATE TABLE order_item;");

            // 清空 order 表
            jdbcTemplate.execute("TRUNCATE TABLE `order`;");

            // 重置 AUTO_INCREMENT
            jdbcTemplate.execute("ALTER TABLE `order` AUTO_INCREMENT = 1;");
            jdbcTemplate.execute("ALTER TABLE order_item AUTO_INCREMENT = 1;");

            // 恢復外鍵檢查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset order and order_item tables", e);
        }
    }

    // 保存訂單到資料庫
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    // 將 Order 轉換為 OrderDTO
    public OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getId());
        orderDTO.setShippingAddress(order.getShippingAddress());
        orderDTO.setPaymentMethod(order.getPaymentMethod().name());

        // 轉換 OrderItem 為 OrderItemDTO 列表
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
        orderDTO.setOrderItems(orderItemDTOs);

        return orderDTO;
    }

    // 將 OrderItem 轉換為 OrderItemDTO
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductId(orderItem.getProduct().getId());
        orderItemDTO.setProductName(orderItem.getProduct().getName());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        orderItemDTO.setSize(orderItem.getSize());
        orderItemDTO.setTotalPrice(orderItem.getTotalPrice());
        return orderItemDTO;
    }
}
