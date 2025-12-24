package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_interactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_user_id", "to_user_id"})
)

public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserInteraction() {}

    public UserInteraction(User fromUser, User toUser, InteractionType type) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getFromUser() { return fromUser; }
    public User getToUser() { return toUser; }
    public InteractionType getType() { return type; }
    public void setType(InteractionType type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}