package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 定義 SecurityScheme
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // 認證方式：HTTP
                .scheme("bearer")               // 使用 Bearer Token
                .bearerFormat("JWT")            // Token 格式為 JWT
                .in(SecurityScheme.In.HEADER)   // 放在 HTTP Header
                .name("Authorization");         // Header 名稱（預設就是 Authorization）

        // 定義 SecurityRequirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth"); // 名稱要和上面 Scheme 的 key 一致

        // 回傳 OpenAPI 組態
        return new OpenAPI()
                .info(new Info()
                        .title("測試用購物網站 API 文件")
                        .version("1.0")
                        .description("這是使用 Springdoc OpenAPI 自動生成的 Swagger 文件"))
                // 加入 Security 設定
                .addSecurityItem(securityRequirement)
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", bearerAuthScheme));
    }
}
