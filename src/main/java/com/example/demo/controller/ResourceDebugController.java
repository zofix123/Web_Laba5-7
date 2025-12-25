package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@RestController
public class ResourceDebugController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/api/debug/resources")
    public ResponseEntity<String> debugResources() {
        StringBuilder sb = new StringBuilder();

        try {
            // 1. Проверяем путь к CSS
            Resource cssResource = resourceLoader.getResource("classpath:static/css/styles.css");
            sb.append("1. CSS resource exists: ").append(cssResource.exists()).append("\n");
            sb.append("   URL: ").append(cssResource.getURL()).append("\n");

            // 2. Пытаемся прочитать CSS
            if (cssResource.exists()) {
                String cssContent = new BufferedReader(new InputStreamReader(cssResource.getInputStream()))
                        .lines()
                        .limit(5)
                        .collect(Collectors.joining("\n"));
                sb.append("   First 5 lines:\n").append(cssContent).append("\n");
            }

            // 3. Проверяем другие ресурсы
            String[] resources = {
                    "classpath:static/js/time_fetcher.js",
                    "classpath:static/img/like.png",
                    "classpath:static/img/dislike.png"
            };

            for (String res : resources) {
                Resource r = resourceLoader.getResource(res);
                sb.append("\n").append(res).append(" exists: ").append(r.exists()).append("\n");
            }

            // 4. Проверяем пути в файловой системе
            sb.append("\n=== File System Paths ===\n");
            String projectDir = System.getProperty("user.dir");
            sb.append("Project dir: ").append(projectDir).append("\n");

            String[] paths = {
                    projectDir + "/src/main/resources/static/css/styles.css",
                    projectDir + "/target/classes/static/css/styles.css",
                    projectDir + "/BOOT-INF/classes/static/css/styles.css"
            };

            for (String path : paths) {
                boolean exists = Files.exists(Paths.get(path));
                sb.append(path).append(": ").append(exists).append("\n");
            }

            return ResponseEntity.ok(sb.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage() + "\n" + sb.toString());
        }
    }

    @GetMapping("/api/debug/direct-css")
    public ResponseEntity<String> getCssDirectly() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/css/styles.css");
            if (resource.exists()) {
                String content = new String(resource.getInputStream().readAllBytes());
                return ResponseEntity.ok()
                        .header("Content-Type", "text/css")
                        .body(content);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("CSS not found in classpath:static/css/styles.css");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}