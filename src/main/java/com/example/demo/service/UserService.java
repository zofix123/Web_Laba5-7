package com.example.demo.service;

import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    // путь для сохранения файлов (относительный путь внутри проекта)
    private static final String UPLOAD_DIR = "uploads/";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Создаем директорию при инициализации сервиса
        try {
            createUploadDirectory();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузок", e);
        }
    }

    private final UserRepository userRepository;
    // Метод для создания директории загрузок
    private void createUploadDirectory() throws IOException {
        Path uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    // Получение абсолютного пути к директории загрузок
    private Path getUploadPath() {
        // Получаем текущую рабочую директорию (корень проекта)
        String currentDir = System.getProperty("user.dir");
        return Paths.get(currentDir, UPLOAD_DIR);
    }

    // Получение веб-пути для доступа к файлу
    private String getWebPath(String filename) {
        return "/" + UPLOAD_DIR + filename;
    }

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
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("юзера с таким id " + id + " не существует");
        }
        userRepository.deleteById(id);
    }


    @Transactional
    public User saveAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        // Проверяем, что файл не пустой
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не должен быть пустым");
        }

        // Проверяем тип файла
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("Поддерживаются только изображения JPG, PNG и GIF");
        }

        // Получаем путь к директории загрузок
        Path uploadPath = getUploadPath();

        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + fileExtension;

        // Сохраняем файл
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // Получаем веб-путь для сохранения в БД
        String avatarPath = getWebPath(newFilename);

        // Удаляем старый аватар, если он существует
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                String oldFilename = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                Path oldFilePath = uploadPath.resolve(oldFilename);
                Files.deleteIfExists(oldFilePath);
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем выполнение
                System.err.println("Ошибка при удалении старого аватара: " + e.getMessage());
            }
        }

        // Сохраняем путь к новому аватару
        user.setAvatar(avatarPath);
        return userRepository.save(user);
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

    // Метод для получения пользователей по роли
    public List<User> findByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    // Метод для получения всех пользователей (с сортировкой)
    public List<User> getAllUsersSorted() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // Метод для обновления роли пользователя
    @Transactional
    public User updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        // Проверяем валидность роли
        List<String> validRoles = Arrays.asList("admin", "user", "moderator");
        if (!validRoles.contains(newRole)) {
            throw new IllegalArgumentException("Недопустимая роль: " + newRole);
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    // Метод для обновления пользователя (администратором)
    @Transactional
    public User updateUserByAdmin(Long userId, String name, String email, LocalDate birth, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }

        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            // Проверяем уникальность email
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && existingUser.get().getId() != userId) {
                throw new EmailAlreadyExistsException("Email уже используется другим пользователем");
            }
            user.setEmail(email);
        }

        if (birth != null) {
            user.setBirth(birth);
            user.setAge(Period.between(birth, LocalDate.now()).getYears());
        }

        if (role != null && !role.trim().isEmpty()) {
            List<String> validRoles = Arrays.asList("admin", "user", "moderator");
            if (!validRoles.contains(role)) {
                throw new IllegalArgumentException("Недопустимая роль: " + role);
            }
            user.setRole(role);
        }

        return userRepository.save(user);
    }
}