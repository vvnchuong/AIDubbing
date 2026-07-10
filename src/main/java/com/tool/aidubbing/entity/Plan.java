package com.tool.aidubbing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Table(name = "plans")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column
    String detail;

    @Column(name = "monthly_minutes", nullable = false)
    Integer monthlyMinutes;

    @Column(nullable = false)
    Integer price;
}