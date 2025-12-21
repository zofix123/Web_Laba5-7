package com.example.demo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import com.example.demo.entity.User;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET для отображения страницы регистрации
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // POST для обработки регистрации
    @PostMapping("/create")
    public String create(User user,
                         HttpSession session,
                         Model model) {
        try {
            User saved = userService.create(user);
            // При регистрации счетчик = 0 (по умолчанию)
            session.setAttribute("user", saved);
            return "redirect:/profile";
        } catch (DataIntegrityViolationException | EmailAlreadyExistsException e) {
            model.addAttribute("name", user.getName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("birth", user.getBirth());
            model.addAttribute("error", "Пользователь с такой почтой уже существует");
            return "register";
        }
    }

    //  Get для отображения страницы входа
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Post Вход пользователя
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        HttpSession session,
                        Model model) {
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            // Пользователь найден - сохраняем в сессию
            User user = userOptional.get();
            // Инкремент счетчика посещений
            user = userService.incrementVisitCount(user.getId());
            session.setAttribute("user", user);
            return "redirect:/profile";
        } else {
            // Пользователь не найден
            model.addAttribute("error", "Пользователь с таким email не найден");
            model.addAttribute("email", email); // сохранение введенного email
            return "login"; //возврат на страницу входа с ошибкой
        }
    }

    // Выход пользователя
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user"); // удаляем пользователя из сессии
        session.invalidate(); // полностью завершаем сессию
        return "redirect:/";
    }

    //Удаление сущности
    @DeleteMapping(path = "{id}")
    @ResponseBody
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    //Возможность обновления емейла и имени сущности
    @PutMapping(path = "{id}")
    @ResponseBody
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String name) {
        userService.update(id, email, name);
    }

    // отдельный эндпоинт для обновления счетчика
    @GetMapping("/increment-visits/{userId}")
    @ResponseBody
    public String incrementVisits(@PathVariable Long userId) {
        User user = userService.incrementVisitCount(userId);
        return "Счетчик посещений пользователя " + user.getName() +
                " увеличен. Текущее значение: " + user.getVisitCount();
    }
}