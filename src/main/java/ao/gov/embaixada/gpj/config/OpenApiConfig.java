package ao.gov.embaixada.gpj.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gpjOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GOP — Gestão de Operações API")
                        .description("API REST para monitoramento de operações do ecossistema digital")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("Ecossistema Digital")
                                .email("dev@ecossistema.local")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .schemaRequirement("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token from Keycloak"));
    }
}
