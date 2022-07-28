package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationUser {
    private String id;
    private String username;
    private String password;
}
