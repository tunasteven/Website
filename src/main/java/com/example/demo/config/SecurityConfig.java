package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.util.JwtAuthenticationEntryPoint;
import com.example.demo.util.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 自定義的用戶詳情服務
    private final CustomUserDetailsService userDetailsService;
    // JWT 請求過濾器
    private final JwtRequestFilter jwtRequestFilter;
    // JWT 認證入口點
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF 保護，因為我們使用的是 JWT（無狀態認證）
                .csrf(csrf -> csrf.disable())
                // 啟用 CORS 支援
                .cors(cors -> cors.configure(http))
                // 配置授權規則
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth/**").permitAll() // 設定 /auth/** 路徑可以匿名訪問
                                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 設定 /api/admin/** 路徑需具有 ADMIN 角色
                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // 設定 /api/user/** 路徑需具有 USER 或 ADMIN 角色
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // 允許 OPTIONS 請求
                                .requestMatchers("/auth/register").permitAll()
                                .anyRequest().authenticated() // 其他所有請求需要驗證
                )
                // 配置例外處理（如未認證時的處理邏輯）
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint) // 使用自定義的 JWT 認證入口點
                )
                // 設定會話管理為無狀態（STATELESS）
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 在 UsernamePasswordAuthenticationFilter 執行之前添加自定義的 JWT 請求過濾器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 建構並返回 SecurityFilterChain
    }

    // 密碼編碼器 Bean，用於加密和驗證用戶密碼
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 認證管理器 Bean，用於處理用戶認證邏輯
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
