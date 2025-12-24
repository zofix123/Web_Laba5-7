package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserInteraction;
import com.example.demo.entity.InteractionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    Optional<UserInteraction> findByFromUser_IdAndToUser_Id(Long fromId, Long toId);

    @Query("""
        select i.toUser
        from UserInteraction i
        where i.fromUser.id = :fromId and i.type = :type
        order by i.createdAt desc
    """)
    List<User> findTargetsByFromUserAndType(Long fromId, InteractionType type);

    // Следующий кандидат после currentId (с учётом скрытых/лайкнутых и исключая себя)
    @Query("""
        select u from User u
        where u.id <> :excludeId
          and u.id > :currentId
          and u.id not in (
              select i.toUser.id from UserInteraction i
              where i.fromUser.id = :excludeId
          )
        order by u.id asc
    """)
    List<User> findNextCandidate(Long excludeId, Long currentId, Pageable pageable);

    // Первый кандидат (если currentId нет)
    @Query("""
        select u from User u
        where u.id <> :excludeId
          and u.id not in (
              select i.toUser.id from UserInteraction i
              where i.fromUser.id = :excludeId
          )
        order by u.id asc
    """)
    List<User> findFirstCandidate(Long excludeId, Pageable pageable);

    // Если не залогинен — просто листаем всех подряд
    @Query("""
        select u from User u
        where u.id > :currentId
        order by u.id asc
    """)
    List<User> findNextAny(Long currentId, Pageable pageable);

    @Query("""
        select u from User u
        order by u.id asc
    """)
    List<User> findFirstAny(Pageable pageable);

    void deleteByFromUser_IdOrToUser_Id(Long fromId, Long toId);
    boolean existsByFromUser_IdOrToUser_Id(Long fromId, Long toId);

    @Modifying
    @Transactional
    @Query("delete from UserInteraction ui where ui.fromUser.id = :userId or ui.toUser.id = :userId")
    void deleteAllForUser(@Param("userId") Long userId);
}
