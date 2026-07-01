package com.example.task_management_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String password;
    private String role;

    @Column(nullable = false)
    private boolean enabled = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

}
