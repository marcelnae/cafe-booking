package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "waiter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    /** BCrypt hash of the 4-digit PIN. Never stored as plain text. */
    @Column(name = "pin_hash", nullable = false, length = 100)
    private String pinHash;

    @Column(nullable = false)
    private boolean active = true;
}
