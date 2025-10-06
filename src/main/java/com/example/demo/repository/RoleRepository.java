package com.example.demo.repository;

import com.example.demo.model.entity.Role;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE role AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

}
