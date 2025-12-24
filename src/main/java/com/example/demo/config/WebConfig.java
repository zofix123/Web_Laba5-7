package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Получаем абсолютный путь к директории загрузок
        String uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir).toAbsolutePath().toString();

        // Маппинг для доступа к загруженным файлам через веб
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/", "file:./uploads/news/");

        // для  Tomcat в IDE:
        // String projectPath = Paths.get("").toAbsolutePath().toString();
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:" + projectPath + "/" + uploadDir + "/");
    }
}