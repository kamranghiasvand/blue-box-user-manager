package com.bluebox;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {
    @Value("${app.url}")
    private String appUrl;
    @Value("${spring.mail.username}")
    private String emailAddress;
}
