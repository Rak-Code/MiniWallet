package com.example.miniwallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	
	 private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
	    
	    @Bean
	    public OpenAPI customOpenAPI() {
	        return new OpenAPI()
	                .info(new Info()
	                        .title("MiniWallet API")
	                        .version("1.0.0")
	                        .description("API for digital wallet management including customer operations, transactions, and wallet services"))
	                .addSecurityItem(new SecurityRequirement()
	                        .addList(SECURITY_SCHEME_NAME))
	                .components(new Components()
	                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
	                                .name(SECURITY_SCHEME_NAME)
	                                .type(SecurityScheme.Type.HTTP)
	                                .scheme("bearer")
	                                .bearerFormat("JWT")
	                                .description("Enter your JWT token")));
	    }

}
