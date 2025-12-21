package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ДОБАВИТЬ этот метод - Spring Data JPA автоматически создаст запрос
    Optional<User> findByEmail(String email);
}