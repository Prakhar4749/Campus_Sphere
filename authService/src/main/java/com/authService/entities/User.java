package com.authService.entities;

import com.authService.enums.Role;
import com.authService.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String enrollmentNo;

    // References to College/Dept ID (Validated via OpenFeign if needed)
    private Long collegeId;
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(columnDefinition = "boolean default false")
    private boolean isEmailVerified;

    @Column(columnDefinition = "boolean default false")
    private boolean isPasswordChangeRequired;
}