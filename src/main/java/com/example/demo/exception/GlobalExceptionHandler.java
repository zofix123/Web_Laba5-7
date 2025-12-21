package com.example.demo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        // текущее время сервера
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        model.addAttribute("serverTime", now.format(formatter));

        model.addAttribute("serverDate", now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        model.addAttribute("serverTimeOnly", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handleEmailAlreadyExists(EmailAlreadyExistsException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "register"; // страница регистрации
    }
}