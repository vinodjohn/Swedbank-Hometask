package com.swedbank.swedbankhometask.common.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the service for the generated OpenAPI document and Swagger UI.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI bankAccountOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Swedbank Account API")
                .description("REST API for multi-currency bank accounts: credit, debit, balance and exchange.")
                .version("v1"));
    }
}
