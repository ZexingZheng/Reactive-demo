package com.example.reactive.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String eventType; // CREATE, UPDATE, DELETE
    private String userId;     // Changed to String to support both Long and MongoDB String IDs
    private String userName;
    private String userEmail;
    private Integer userAge;
    private LocalDateTime timestamp;

    public static UserEvent create(Long userId, String name, String email, Integer age) {
        return new UserEvent("CREATE", String.valueOf(userId), name, email, age, LocalDateTime.now());
    }

    public static UserEvent update(Long userId, String name, String email, Integer age) {
        return new UserEvent("UPDATE", String.valueOf(userId), name, email, age, LocalDateTime.now());
    }

    public static UserEvent delete(Long userId) {
        return new UserEvent("DELETE", String.valueOf(userId), null, null, null, LocalDateTime.now());
    }
}
