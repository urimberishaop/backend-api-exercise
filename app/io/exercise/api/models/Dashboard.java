package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel{
    private String name;
    private String description;
    private String parentId;
    private List<String> readACL = new ArrayList<>();
    private List<String> writeACL = new ArrayList<>();
    private List<Content> content = new ArrayList<>();
}
