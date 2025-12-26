package com.example.demo.controller;

import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import com.example.demo.entity.User;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // GET для отображения страницы регистрации
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // POST для обработки регистрации с проверкой пароля
    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam String birth,
                         HttpSession session,
                         Model model) {

        // Проверка длины пароля
        if (password.length() < 4) {
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("error", "Пароль должен содержать минимум 4 символа");
            return "register";
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }

        try {
            // Создание пользователя
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setBirth(java.time.LocalDate.parse(birth));

            User saved = userService.create(user);
            session.setAttribute("user", saved);
            return "redirect:/profile";
        } catch (DataIntegrityViolationException | EmailAlreadyExistsException e) {
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("error", "Пользователь с такой почтой уже существует");
            return "register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("error", e.getMessage());
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
                        @RequestParam String password, // Добавлен параметр пароля
                        HttpSession session,
                        Model model) {
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Проверка пароля
            if (user.getPassword() != null && password.equals(user.getPassword())) {
                // Пароль верный - инкремент счетчика и вход
                user = userService.incrementVisitCount(user.getId());
                session.setAttribute("user", user);
                return "redirect:/profile";
            } else {
                // Неверный пароль
                model.addAttribute("error", "Неверный пароль");
                model.addAttribute("email", email); // сохранение введенного email
                return "login"; // возврат на страницу входа с ошибкой
            }
        } else {
            // Пользователь не найден
            model.addAttribute("error", "Пользователь с таким email не найден");
            model.addAttribute("email", email); // сохранение введенного email
            return "login"; // возврат на страницу входа с ошибкой
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/users/login";
        }
        return "change-password";
    }

    // Обработка смены пароля
    @PostMapping("/change-password")
    public String changePassword(@RequestParam Long userId,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmNewPassword,
                                 HttpSession session,
                                 Model model) {

        try {
            User user = userService.changePassword(userId, oldPassword, newPassword, confirmNewPassword);
            session.setAttribute("user", user);
            model.addAttribute("success", "Пароль успешно изменен");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "change-password";
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
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String password,
                       @RequestParam(required = false) String confirmPassword) {
        userService.update(id, email, name, password, confirmPassword);
    }

    // отдельный эндпоинт для обновления счетчика
    @GetMapping("/increment-visits/{userId}")
    @ResponseBody
    public String incrementVisits(@PathVariable Long userId) {
        User user = userService.incrementVisitCount(userId);
        return "Счетчик посещений пользователя " + user.getName() +
                " увеличен. Текущее значение: " + user.getVisitCount();
    }


    // метод для отображения страницы загрузки аватара
    @GetMapping("/upload-avatar")
    public String showUploadAvatarPage(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/users/login";
        }
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "upload-avatar";
    }

    // метод для обработки загрузки аватара
    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               HttpSession session,
                               Model model,
                               HttpServletRequest request) {
        if (session.getAttribute("user") == null) {
            return "redirect:/users/login";
        }

        User sessionUser = (User) session.getAttribute("user");

        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Пожалуйста, выберите файл");
                return "upload-avatar";
            }

            // Сохраняем аватар и получаем обновленного пользователя
            User updatedUser = userService.saveAvatar(sessionUser.getId(), file);
            session.setAttribute("user", updatedUser);
            model.addAttribute("success", "Аватар успешно загружен");

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (IOException e) {
            model.addAttribute("error", "Ошибка при сохранении файла: " + e.getMessage());
        }

        return "upload-avatar";
    }

    // Страница администратора
    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        // Проверяем, является ли пользователь администратором
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return "redirect:/profile";
        }

        List<User> users = userService.getAllUsersSorted();
        long totalUsers = users.size();
        long adminCount = users.stream()
                .filter(u -> "admin".equals(u.getRole()))
                .count();
        long moderatorCount = users.stream()
                .filter(u -> "moderator".equals(u.getRole()))
                .count();
        long userCount = users.stream()
                .filter(u -> "user".equals(u.getRole()))
                .count();

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("moderatorCount", moderatorCount);
        model.addAttribute("userCount", userCount);

        return "admin";
    }

    // Удаление пользователя (администратором)
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return "redirect:/profile";
        }

        // Нельзя удалить самого себя
        if (currentUser.getId() == id) {
            redirectAttributes.addFlashAttribute("error", "Вы не можете удалить свой собственный аккаунт");
            return "redirect:/users/admin";
        }

        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении пользователя: " + e.getMessage());
        }

        return "redirect:/users/admin";
    }

    // Обновление роли пользователя
    @PostMapping("/admin/update-role/{id}")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return "redirect:/profile";
        }

        // Нельзя изменить свою собственную роль
        if (currentUser.getId() == id) {
            redirectAttributes.addFlashAttribute("error", "Вы не можете изменить свою собственную роль");
            return "redirect:/users/admin";
        }

        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "Роль пользователя обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении роли: " + e.getMessage());
        }

        return "redirect:/users/admin";
    }

    // Обновление данных пользователя (администратором)
    @PostMapping("/admin/update/{id}")
    public String updateUserByAdmin(@PathVariable Long id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String birth,
                                    @RequestParam(required = false) String role,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return "redirect:/profile";
        }

        try {
            LocalDate birthDate = null;
            if (birth != null && !birth.trim().isEmpty()) {
                birthDate = LocalDate.parse(birth);
            }

            userService.updateUserByAdmin(id, name, email, birthDate, role);
            redirectAttributes.addFlashAttribute("success", "Данные пользователя обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }

        return "redirect:/users/admin";
    }

}