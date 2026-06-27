package com.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 文档配置
 * 访问地址: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI libraryOpenAPI() {
        final String securitySchemeName = "JWT Bearer Token";

        return new OpenAPI()
                .info(new Info()
                        .title("高校图书馆借阅与座位管理系统 API")
                        .description("Spring Boot 3.2 + Vue 3 前后端分离图书馆管理系统 API 文档")
                        .version("v5.2")
                        .contact(new Contact()
                                .name("Library System")
                                .email("admin@library.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Token 认证，登录后获取 Token 填入此处")));
    }
}
