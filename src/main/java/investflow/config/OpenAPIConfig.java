package investflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenAPIConfig {

    @Bean
    @Profile("doc")
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("InvestFlow API - Documentation")
                        .version("1.0.0-DOC")
                        .description("Documentation environment using H2 database")
                        .contact(new Contact()
                                .name("Herik Erbeth")
                                .url("https://github.com/herikerbeth")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Documentation server"));
    }
}
