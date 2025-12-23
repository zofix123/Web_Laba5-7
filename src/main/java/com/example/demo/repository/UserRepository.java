package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // следующий пользователь по id (для стрелки вправо)
    Optional<User> findFirstByIdGreaterThanAndIdNotOrderByIdAsc(Long id, Long excludeId);

    // предыдущий пользователь по id (для стрелки влево)
    Optional<User> findFirstByIdLessThanAndIdNotOrderByIdDesc(Long id, Long excludeId);

    // первый пользователь (если мы только зашли и id не задан)
    Optional<User> findFirstByIdNotOrderByIdAsc(Long excludeId);

    // если никто не залогинен
    Optional<User> findFirstByOrderByIdAsc();
}
