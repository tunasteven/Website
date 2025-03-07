package com.example.demo.service;

import com.example.demo.dto.UserRegistrationRequest;
import com.example.demo.model.entity.Role;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.User.Gender;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    // 註冊新用戶
    public User registerNewUser(UserRegistrationRequest registrationRequest) {
        validateUserExists(registrationRequest.getUsername(), registrationRequest.getEmail());

        User user = buildUserFromRequest(registrationRequest);

        // 自動分配 ROLE_USER
        Role userRole = getUserRole();
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    private void validateUserExists(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }

    private User buildUserFromRequest(UserRegistrationRequest registrationRequest) {
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setPhone(registrationRequest.getPhone());
        user.setBirthdate(parseBirthdate(registrationRequest.getBirthdate()));
        user.setTitle(parseGender(registrationRequest.getTitle()));
        user.setCity(registrationRequest.getCity());
        user.setAddress(registrationRequest.getAddress());
        user.setEnabled(true);
        return user;
    }

    private LocalDate parseBirthdate(String birthdate) {
        if (StringUtils.hasText(birthdate)) {
            return LocalDate.parse(birthdate, DATE_FORMATTER);
        }
        throw new IllegalArgumentException("Invalid birthdate: " + birthdate);
    }

    private Gender parseGender(String title) {
        if (StringUtils.hasText(title)) {
            try {
                return Gender.valueOf(title.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid title: " + title);
            }
        }
        throw new IllegalArgumentException("Title cannot be null or empty");
    }

    private Role getUserRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found: ROLE_USER"));
    }

    // 獲取用戶根據 Email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 更新用戶
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // 獲取所有用戶
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 獲取用戶根據 ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 刪除用戶
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
