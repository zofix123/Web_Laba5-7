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
    public void update(Long id, String email, String name) {
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
    }
}
