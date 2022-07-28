package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String parentId;
    private Timestamp createdAt = Timestamp.from(Instant.now());
    private List<String> readACL;
    private List<String> writeACL;
}
