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

@Controller
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Получение всех сущностей
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    //Добавление сущности
    @PostMapping("/create")
    public String create(User user,
                         HttpSession session,
                         Model model) {
        try {
            User saved = userService.create(user);
            session.setAttribute("user", saved);
            return "redirect:/profile";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Пользователь с такой почтой уже существует");
            return "register";
        }
    }

    //Удаление сущности
    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    //Возможность обновления емейла и имени сущности
    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String name) {
        userService.update(id, email, name);
    }
}
