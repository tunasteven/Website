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

import java.io.InputStream;
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
    private JdbcTemplate jdbcTemplate; // 用於直接操作子表

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🧹 開始清空資料並重置 ID...");

        // 🔹 先刪子表資料，避免 FK 衝突
        jdbcTemplate.execute("DELETE FROM order_item");
        jdbcTemplate.execute("ALTER TABLE order_item AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("ALTER TABLE user_roles AUTO_INCREMENT = 1");

        // 🔹 再刪父表資料
        jdbcTemplate.execute("DELETE FROM `order`");
        jdbcTemplate.execute("ALTER TABLE `order` AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM user");
        jdbcTemplate.execute("ALTER TABLE user AUTO_INCREMENT = 1");

        jdbcTemplate.execute("DELETE FROM role");
        jdbcTemplate.execute("ALTER TABLE role AUTO_INCREMENT = 1");

        System.out.println("🧹 資料已清空，ID 重置完成。");

        // 🔹 讀取 seed-users.json
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("seed-users.json").getInputStream();
        List<SeedUser> seedUsers = objectMapper.readValue(inputStream, new TypeReference<List<SeedUser>>() {});
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 🔹 重新建立使用者與角色
        for (SeedUser su : seedUsers) {
            Set<Role> roles = su.getRoles().stream()
                    .map(roleName -> roleRepository.save(new Role(roleName)))
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
