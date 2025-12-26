package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.servlet.context-path:}") // пусто по умолчанию
    private String contextPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("Context path: '" + contextPath + "'");

        // Определяем физический путь
        String uploadDir;
        String catalinaBase = System.getProperty("catalina.base");

        if (catalinaBase != null && !catalinaBase.isEmpty()) {
            // Tomcat
            uploadDir = catalinaBase + "/webapps/uploads";
        } else {
            // IDE
            uploadDir = System.getProperty("user.dir") + "/uploads";
        }

        System.out.println("Physical upload dir: " + uploadDir);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}