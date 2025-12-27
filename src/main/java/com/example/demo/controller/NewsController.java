package com.example.demo.controller;

import com.example.demo.entity.News;
import com.example.demo.entity.User;
import com.example.demo.service.NewsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // Показать все новости
    @GetMapping
    public String showAllNews(Model model) {
        List<News> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        model.addAttribute("formatter", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        return "news-list";
    }

    // Показать форму создания новости
    @GetMapping("/create")
    public String showCreateNewsForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || (!user.getRole().equals("admin") && !user.getRole().equals("moderator"))) {
            return "redirect:/news";
        }
        return "news-create";
    }

    // Создать новость
    @PostMapping("/create")
    public String createNews(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || (!user.getRole().equals("admin") && !user.getRole().equals("moderator"))) {
            return "redirect:/news";
        }

        News news = new News();
        news.setTitle(title);
        news.setContent(content);

        try {
            newsService.createNews(news, user, imageFile);
            redirectAttributes.addFlashAttribute("success", "Новость успешно создана");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке изображения: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании новости: " + e.getMessage());
        }

        return "redirect:/news";
    }

    // Показать форму редактирования новости
    @GetMapping("/edit/{id}")
    public String showEditNewsForm(@PathVariable Long id,
                                   HttpSession session,
                                   Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/news";
        }

        News news = newsService.getNewsById(id)
                .orElseThrow(() -> new IllegalStateException("Новость не найдена"));

        // Проверка прав
        boolean canEdit = user.getRole().equals("admin") ||
                user.getRole().equals("moderator") ||
                news.getAuthor().getId() == user.getId();

        if (!canEdit) {
            return "redirect:/news";
        }

        model.addAttribute("news", news);
        return "news-edit";
    }

    // Обновить новость
    @PostMapping("/edit/{id}")
    public String updateNews(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/news";
        }

        try {
            newsService.updateNews(id, title, content, user, imageFile, removeImage);
            redirectAttributes.addFlashAttribute("success", "Новость успешно обновлена");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке изображения: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении новости: " + e.getMessage());
        }

        return "redirect:/news";
    }

    // Удалить новость
    @PostMapping("/delete/{id}")
    public String deleteNews(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/news";
        }

        // Проверяем права
        News news = newsService.getNewsById(id).orElse(null);
        if (news != null) {
            boolean canDelete = user.getRole().equals("admin") ||
                    user.getRole().equals("moderator") ||
                    news.getAuthor().getId() == user.getId();

            if (!canDelete) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для удаления этой новости");
                return "redirect:/news";
            }
        }

        try {
            newsService.deleteNews(id, user);
            redirectAttributes.addFlashAttribute("success", "Новость успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении новости: " + e.getMessage());
        }

        return "redirect:/news";
    }
}