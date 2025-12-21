package com.example.demo.service;

import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(User user) {
        if (user.getBirth() == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }

        // Проверка длины пароля
        if (user.getPassword().length() < 4) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
        }

        user.setAge(Period.between(user.getBirth(), LocalDate.now()).getYears());
        try {
            return userRepository.save(user);
        }
        catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException("Пользователь с такой почтой уже существует");
        }
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("юзера с таким id " + id + " не существует");
        }
    }

    @Transactional
    public void update(Long id, String email, String name, String password, String confirmPassword) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("юзера с таким id " + id + " не существует");
        }
        User user = userRepository.findById(id).get();

        if (email != null && !email.equals(user.getEmail())) {
            try {
                user.setEmail(email);
            }
            catch (DataIntegrityViolationException e) {
                throw new EmailAlreadyExistsException("Пользователь с такой почтой уже существует");
            }
        }
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
        }
        if (password != null && !password.trim().isEmpty()) {
            // Проверка длины при обновлении
            if (password.length() < 4) {
                throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
            }
            // Проверка совпадения паролей (если передано confirmPassword)
            if (confirmPassword != null && !password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            user.setPassword(password);
        }
    }

    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword, String confirmNewPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        // Проверка старого пароля
        if (!oldPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Неверный старый пароль");
        }

        // Проверка длины нового пароля
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("Новый пароль должен содержать минимум 4 символа");
        }

        // Проверка совпадения новых паролей
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("Новые пароли не совпадают");
        }

        // Проверка, что новый пароль отличается от старого
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("Новый пароль должен отличаться от старого");
        }

        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    //  Поиск пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // метод для увеличения счетчика посещений
    @Transactional
    public User incrementVisitCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        user.incrementVisitCount(); // увеличиваем счетчик
        return userRepository.save(user); // сохраняем изменения
    }

    // метод для получения пользователя с увеличением счетчика
    @Transactional
    public User getAndIncrementVisitCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        user.incrementVisitCount(); // увеличиваем счетчик
        return userRepository.save(user); // сохраняем и возвращаем
    }
}