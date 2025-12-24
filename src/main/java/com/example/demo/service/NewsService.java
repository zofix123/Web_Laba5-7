package com.example.demo.service;

import com.example.demo.entity.News;
import com.example.demo.entity.User;
import com.example.demo.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private static final String NEWS_UPLOAD_DIR = "uploads/news/";

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
        // Создаем директорию при инициализации сервиса
        try {
            createNewsUploadDirectory();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузок новостей", e);
        }
    }

    private void createNewsUploadDirectory() throws IOException {
        Path uploadPath = getNewsUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private Path getNewsUploadPath() {
        String currentDir = System.getProperty("user.dir");
        return Paths.get(currentDir, NEWS_UPLOAD_DIR);
    }

    private String getNewsWebPath(String filename) {
        return "/" + NEWS_UPLOAD_DIR + filename;
    }

    private String saveNewsImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Проверяем тип файла
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/gif") &&
                        !contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Поддерживаются только изображения JPG, PNG, GIF и WebP");
        }

        // Получаем путь к директории загрузок
        Path uploadPath = getNewsUploadPath();

        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + fileExtension;

        // Сохраняем файл
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // Возвращаем веб-путь
        return getNewsWebPath(newFilename);
    }

    private void deleteNewsImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                Path filePath = getNewsUploadPath().resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Ошибка при удалении изображения новости: " + e.getMessage());
            }
        }
    }

    public List<News> getAllNews() {
        return newsRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    @Transactional
    public News createNews(News news, User author, MultipartFile imageFile) throws IOException {
        news.setAuthor(author);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());

        // Сохраняем изображение, если оно есть
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveNewsImage(imageFile);
            news.setImagePath(imagePath);
        }

        return newsRepository.save(news);
    }

    @Transactional
    public News updateNews(Long id, String title, String content, User updater, MultipartFile imageFile) throws IOException {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Новость не найдена"));

        Long updaterId = updater.getId();
        Long authorId = news.getAuthor().getId();

        boolean isAdmin = "admin".equals(updater.getRole());
        boolean isModerator = "moderator".equals(updater.getRole());
        boolean isAuthor = updaterId.equals(authorId);

        if (!isAdmin && !isModerator && !isAuthor) {
            throw new IllegalStateException("У вас нет прав для редактирования этой новости");
        }

        news.setTitle(title);
        news.setContent(content);
        news.setUpdatedAt(LocalDateTime.now());

        // Обновляем изображение, если новое загружено
        if (imageFile != null && !imageFile.isEmpty()) {
            // Удаляем старое изображение
            deleteNewsImage(news.getImagePath());

            // Сохраняем новое изображение
            String imagePath = saveNewsImage(imageFile);
            news.setImagePath(imagePath);
        }

        return newsRepository.save(news);
    }

    @Transactional
    public void deleteNews(Long id, User deleter) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Новость не найдена"));

        Long deleterId = deleter.getId();
        Long authorId = news.getAuthor().getId();

        boolean isAdmin = "admin".equals(deleter.getRole());
        boolean isModerator = "moderator".equals(deleter.getRole());
        boolean isAuthor = deleterId.equals(authorId);

        if (!isAdmin && !isModerator && !isAuthor) {
            throw new IllegalStateException("У вас нет прав для удаления этой новости");
        }

        // Удаляем связанное изображение
        deleteNewsImage(news.getImagePath());

        newsRepository.delete(news);
    }
}