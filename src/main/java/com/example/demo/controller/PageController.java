package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Controller
public class PageController {

    private final UserRepository userRepository;

    public PageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        addServerTimeToModel(model);
        return "main";
    }

    // чтобы не было дублей страниц:
    @GetMapping("/register")
    public String registerRedirect() {
        return "redirect:/users/register";
    }

    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/users/login";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        addServerTimeToModel(model);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/main")
    public String mainPage(
            @RequestParam(required = false) Long id,
            HttpSession session,
            Model model
    ) {
        addServerTimeToModel(model);

        User currentUser = (User) session.getAttribute("user");
        Long excludeId = (currentUser != null) ? currentUser.getId() : null;

        User candidate = pickCandidate(id, excludeId);
        model.addAttribute("candidate", candidate);

        // подготовим id для стрелок
        Long prevId = null;
        Long nextId = null;

        if (candidate != null) {
            long candId = candidate.getId();

            if (excludeId != null) {
                prevId = userRepository
                        .findFirstByIdLessThanAndIdNotOrderByIdDesc(candId, excludeId)
                        .map(User::getId)
                        .orElse(null);

                nextId = userRepository
                        .findFirstByIdGreaterThanAndIdNotOrderByIdAsc(candId, excludeId)
                        .map(User::getId)
                        .orElse(null);
            } else {
                // если никто не залогинен — листаем всех
                prevId = userRepository.findAll().stream()
                        .filter(u -> u.getId() < candId)
                        .map(User::getId)
                        .max(Long::compareTo)
                        .orElse(null);

                nextId = userRepository.findAll().stream()
                        .filter(u -> u.getId() > candId)
                        .map(User::getId)
                        .min(Long::compareTo)
                        .orElse(null);
            }
        }

        model.addAttribute("prevId", prevId);
        model.addAttribute("nextId", nextId);

        return "main";
    }

    private User pickCandidate(Long id, Long excludeId) {
        if (excludeId != null) {
            if (id != null) {
                Optional<User> byId = userRepository.findById(id);
                if (byId.isPresent() && byId.get().getId() != excludeId) {
                    return byId.get();
                }
            }
            return userRepository.findFirstByIdNotOrderByIdAsc(excludeId).orElse(null);
        } else {
            if (id != null) {
                return userRepository.findById(id).orElse(null);
            }
            return userRepository.findFirstByOrderByIdAsc().orElse(null);
        }
    }

    private void addServerTimeToModel(Model model) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        model.addAttribute("serverTime", sdf.format(new Date()));
    }
}
