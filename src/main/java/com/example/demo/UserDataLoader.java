package com.example.demo;

import com.example.demo.model.entity.Role;
import com.example.demo.model.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🧹 開始清空資料並重置 ID...");

        // 🔹 自動偵測資料庫類型
        String databaseType = detectDatabaseType();
        System.out.println("🗄️ 偵測到資料庫: " + databaseType);

        // 🔹 根據資料庫類型執行不同的清空邏輯
        if ("PostgreSQL".equals(databaseType)) {
            resetPostgreSQL();
        } else if ("MySQL".equals(databaseType)) {
            resetMySQL();
        } else {
            throw new UnsupportedOperationException("不支援的資料庫類型: " + databaseType);
        }

        System.out.println("🧹 資料已清空，ID 重置完成。");

        // 🔹 讀取 seed-users.json
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("seed-users.json").getInputStream();
        List<SeedUser> seedUsers = objectMapper.readValue(inputStream, new TypeReference<List<SeedUser>>() {});
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 🔹 重新建立使用者與角色
        for (SeedUser su : seedUsers) {
            Set<Role> roles = su.getRoles().stream()
                    .map(roleName -> {
                        return roleRepository.findByName(roleName)
                                .orElseGet(() -> roleRepository.save(new Role(roleName)));
                    })
                    .collect(Collectors.toSet());

            User user = new User();
            user.setUsername(su.getUsername());
            user.setEmail(su.getEmail());
            user.setPassword(encoder.encode(su.getPassword()));
            user.setPhone(su.getPhone());
            user.setBirthdate(LocalDate.parse(su.getBirthdate()));
            user.setTitle(User.Gender.valueOf(su.getTitle()));
            user.setCity(su.getCity());
            user.setAddress(su.getAddress());
            user.setEnabled(su.isEnabled());
            user.setRoles(roles);

            userRepository.save(user);
        }

        System.out.println("✅ 測試使用者資料已重新載入完成！");
    }

    /**
     * 偵測資料庫類型
     */
    private String detectDatabaseType() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            if (productName.toLowerCase().contains("postgresql")) {
                return "PostgreSQL";
            } else if (productName.toLowerCase().contains("mysql")) {
                return "MySQL";
            }
            return productName;
        }
    }

    /**
     * PostgreSQL 資料重置
     */
    private void resetPostgreSQL() {
        // PostgreSQL 使用 TRUNCATE ... RESTART IDENTITY CASCADE
        jdbcTemplate.execute("TRUNCATE TABLE order_item RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE user_roles RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE role RESTART IDENTITY CASCADE");
        System.out.println("✅ PostgreSQL 資料重置完成");
    }

    /**
     * MySQL 資料重置
     */
    private void resetMySQL() {
        // MySQL 需要先關閉外鍵檢查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.execute("DELETE FROM order_item");
        jdbcTemplate.execute("ALTER TABLE order_item AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("ALTER TABLE user_roles AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("ALTER TABLE orders AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM role");
        jdbcTemplate.execute("ALTER TABLE role AUTO_INCREMENT = 1");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        System.out.println("✅ MySQL 資料重置完成");
    }

    private static class SeedUser {
        private String username;
        private String email;
        private String password;
        private String phone;
        private String birthdate;
        private String title;
        private String city;
        private String address;
        private boolean enabled;
        private List<String> roles;

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
        public String getBirthdate() { return birthdate; }
        public String getTitle() { return title; }
        public String getCity() { return city; }
        public String getAddress() { return address; }
        public boolean isEnabled() { return enabled; }
        public List<String> getRoles() { return roles; }
    }
}