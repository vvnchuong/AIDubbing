package com.tool.aidubbing.entity;

import com.tool.aidubbing.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "google_id", unique = true, nullable = false)
    String googleId;

    @Column(nullable = false)
    String email;

    String name;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column(name = "plan_id")
    Long planId;

    @Column(name = "quota_minutes_left")
    Integer quotaMinutesLeft = 10;

    @Column(name = "quota_reset_at")
    Instant quotaResetAt;

    @Enumerated(EnumType.STRING)
    UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        role = UserRole.USER;
    }
}