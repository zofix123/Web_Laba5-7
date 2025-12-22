package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@SessionAttributes("user") //для управления сессией
public class PageController {

    private final UserService userService;

    public PageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        addServerTimeToModel(model);
        // Добавляем пользователя в модель, если он есть в сессии
        if (session.getAttribute("user") != null) {
            model.addAttribute("user", session.getAttribute("user"));
        }
        return "index";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        addServerTimeToModel(model);
        return "register";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        addServerTimeToModel(model);
        return "login";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        addServerTimeToModel(model);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/users/login";
        }

        //Отображение счетчика посещений в профиле
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", "admin".equals(user.getRole()));
        return "profile";
    }

    private void addServerTimeToModel(Model model) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        model.addAttribute("serverTime", sdf.format(new Date()));
    }

}