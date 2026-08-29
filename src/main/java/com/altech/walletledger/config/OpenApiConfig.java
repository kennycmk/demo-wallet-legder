package com.altech.walletledger.config;

import com.altech.walletledger.constant.AppConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletLedgerOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Ledger API")
                        .version("0.0.1")
                        .description("Wallet ledger backend service"))
                .components(new Components().addSecuritySchemes(AppConstants.JWT_SCHEME, bearer));
    }
}
