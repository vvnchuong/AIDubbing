package com.tool.aidubbing.entity;

import com.tool.aidubbing.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(nullable = false)
    Integer amount;

    @Column(nullable = false, unique = true)
    String code;

    @Column(name = "plan_id", nullable = false)
    Long planId;

    @Enumerated(EnumType.STRING)
    TransactionStatus status;

    String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}