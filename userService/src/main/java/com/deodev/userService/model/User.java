package com.deodev.userService.model;

import com.deodev.userService.model.enums.UserStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
public class User {

    private UUID id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private UserStatus status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
