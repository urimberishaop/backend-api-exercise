package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel {
    @NotEmpty(message = "cannot be empty!")
    private String name;
    private String description;
    private ObjectId parentId;
    private List<String> readACL = new ArrayList<>();
    private List<String> writeACL = new ArrayList<>();
}
