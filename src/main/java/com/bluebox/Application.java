package com.bluebox;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);


    public static void main(String[] args) {
        var run = SpringApplication.run(Application.class, args);
        var environment = run.getEnvironment();
        var context = environment.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(context))
            context = "";
        var port = environment.getProperty("server.port");
        if (!StringUtils.hasText(port))
            port = "8080";
        var base = "http://localhost:" + port + context;
        var path = environment.getProperty("springfox.documentation.swagger.v2.path");
        if (!StringUtils.hasText(path))
            path = "";
        var json = base + path;
        var ui = base + "/swagger-ui/index.html";
        LOGGER.info("resource json address is {}", json);
        LOGGER.info("resource ui address is {}", ui);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
}
