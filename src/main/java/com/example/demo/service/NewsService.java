package com.example.demo.service;

import com.example.demo.entity.News;
import com.example.demo.entity.User;
import com.example.demo.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<News> getAllNews() {
        return newsRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    @Transactional
    public News createNews(News news, User author) {
        news.setAuthor(author);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        return newsRepository.save(news);
    }

    @Transactional
    public News updateNews(Long id, String title, String content, User updater) {
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

        return newsRepository.save(news);
    }

    @Transactional
    public void deleteNews(Long id, User deleter) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Новость не найдена"));

        // Проверяем права: только администратор или модератор может удалять любые новости,
        // автор может удалять только свои новости
        Long deleterId = deleter.getId();
        Long authorId = news.getAuthor().getId();

        boolean isAdmin = "admin".equals(deleter.getRole());
        boolean isModerator = "moderator".equals(deleter.getRole());
        boolean isAuthor = deleterId.equals(authorId);

        if (!isAdmin && !isModerator && !isAuthor) {
            throw new IllegalStateException("У вас нет прав для удаления этой новости");
        }

        newsRepository.delete(news);
    }
}