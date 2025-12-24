package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.UserInteractionRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InteractionService {

    private final UserRepository userRepository;
    private final UserInteractionRepository interactionRepository;

    public InteractionService(UserRepository userRepository, UserInteractionRepository interactionRepository) {
        this.userRepository = userRepository;
        this.interactionRepository = interactionRepository;
    }

    @Transactional
    public void setInteraction(Long fromId, Long toId, InteractionType type) {
        if (fromId == null) throw new IllegalStateException("Нет авторизации");
        if (toId == null) throw new IllegalArgumentException("candidateId обязателен");
        if (fromId.equals(toId)) return; // сам себя не лайкаем

        User from = userRepository.findById(fromId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        User to = userRepository.findById(toId)
                .orElseThrow(() -> new IllegalStateException("Кандидат не найден"));

        Optional<UserInteraction> existing = interactionRepository.findByFromUser_IdAndToUser_Id(fromId, toId);
        if (existing.isPresent()) {
            existing.get().setType(type); // поменяли реакцию (например лайк -> дизлайк)
        } else {
            interactionRepository.save(new UserInteraction(from, to, type));
        }
    }

    public User pickCandidate(Long excludeId, Long currentId) {
        if (excludeId == null) {
            // не залогинен — просто листаем всех
            if (currentId == null) {
                return interactionRepository.findFirstAny(PageRequest.of(0,1)).stream().findFirst().orElse(null);
            }
            return interactionRepository.findNextAny(currentId, PageRequest.of(0,1)).stream().findFirst().orElse(null);
        }

        if (currentId == null) {
            return interactionRepository.findFirstCandidate(excludeId, PageRequest.of(0,1)).stream().findFirst().orElse(null);
        }

        return interactionRepository.findNextCandidate(excludeId, currentId, PageRequest.of(0,1)).stream().findFirst().orElse(null);
    }
}
