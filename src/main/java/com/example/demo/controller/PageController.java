package com.example.demo.controller;

import com.example.demo.entity.InteractionType;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserInteractionRepository;
import com.example.demo.service.InteractionService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;

@Controller
public class PageController {

    private final UserRepository userRepository;
    private final UserInteractionRepository interactionRepository;
    private final InteractionService interactionService;
    private final UserService userService;

    public PageController(UserRepository userRepository,
                          UserInteractionRepository interactionRepository,
                          InteractionService interactionService,
                          UserService userService) {
        this.userRepository = userRepository;
        this.interactionRepository = interactionRepository;
        this.interactionService = interactionService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/main";
    }


    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        addServerTimeToModel(model);

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/users/login";

        List<User> likedUsers = interactionRepository.findTargetsByFromUserAndType(sessionUser.getId(), InteractionType.LIKE);

        model.addAttribute("user", sessionUser);
        model.addAttribute("likedUsers", likedUsers);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam LocalDate birth,
                                @RequestParam String gender,
                                @RequestParam String city,
                                HttpSession session,
                                Model model) {

        addServerTimeToModel(model);

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/users/login";

        try {
            User updatedUser = userService.updateProfile(sessionUser.getId(), name, birth, gender, city);
            session.setAttribute("user", updatedUser);

            model.addAttribute("success", "Данные профиля обновлены");
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }

        List<User> likedUsers = interactionRepository.findTargetsByFromUserAndType(sessionUser.getId(), InteractionType.LIKE);

        model.addAttribute("user", sessionUser);
        model.addAttribute("likedUsers", likedUsers);
        return "profile";
    }

    @GetMapping("/main")
    public String mainPage(@RequestParam(required = false) Long id,
                           @RequestParam(required = false) String toast,
                           HttpSession session,
                           Model model) {
        addServerTimeToModel(model);

        User currentUser = (User) session.getAttribute("user");
        Long excludeId = (currentUser != null) ? currentUser.getId() : null;

        User candidate = null;
        if (id != null) {
            candidate = userRepository.findById(id).orElse(null);
            if (candidate != null && excludeId != null && candidate.getId() == excludeId) {
                candidate = null;
            }
            if (candidate != null && excludeId != null) {
                // если уже есть взаимодействие — считаем его “недоступным”
                boolean alreadyInteracted = interactionRepository
                        .findByFromUser_IdAndToUser_Id(excludeId, candidate.getId())
                        .isPresent();
                if (alreadyInteracted) candidate = null;
            }
        }

        if (candidate == null) {
            candidate = interactionService.pickCandidate(excludeId, null);
        }

        model.addAttribute("candidate", candidate);
        model.addAttribute("toast", toast); // для уведомления
        return "main";
    }

    @PostMapping("/main/like")
    public String like(@RequestParam Long candidateId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/users/login";

        interactionService.setInteraction(currentUser.getId(), candidateId, InteractionType.LIKE);

        User next = interactionService.pickCandidate(currentUser.getId(), candidateId);
        if (next == null) return "redirect:/main?toast=empty";

        return "redirect:/main?id=" + next.getId() + "&toast=liked";
    }

    @PostMapping("/main/dislike")
    public String dislike(@RequestParam Long candidateId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/users/login";

        interactionService.setInteraction(currentUser.getId(), candidateId, InteractionType.DISLIKE);

        User next = interactionService.pickCandidate(currentUser.getId(), candidateId);
        if (next == null) return "redirect:/main?toast=empty";

        return "redirect:/main?id=" + next.getId() + "&toast=disliked";
    }

    private void addServerTimeToModel(Model model) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        model.addAttribute("serverTime", sdf.format(new Date()));
    }
}