package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel {
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotEmpty
    private String parentId;
    private List<String> readACL = new ArrayList<>();
    private List<String> writeACL = new ArrayList<>();
}
