package haivo.chatbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("haivoDev Assistant")
                        .version("1.0.0")
                        .description("Đây là một ứng dụng chatbot đơn giản mã nguồn mở. Dưới đây là là tài liệu API trong dự án"+
                                     "Bạn có thể đóng góp hoặc xem mã nguồn tại GitHub.")
                        .contact(new Contact()
                                .name("haivoDev")
                                .url("https://github.com/haivoDA22TTD/Spring_AI")
                                .email("vochihai7@gmail.com")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));

    }
}
