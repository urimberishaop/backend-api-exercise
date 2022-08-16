package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class Dashboard extends BaseModel {
    @NotNull(message = "cannot be empty!")
    @Size(min = 3, max = 25)
    private String name;
    private String description;
    private ObjectId parentId;
    private List<String> readACL = new ArrayList<>();
    private List<String> writeACL = new ArrayList<>();
    @BsonIgnore
    private List<Content> items = new ArrayList<>();
    @BsonIgnore
    @BsonProperty("children")
    private List<Dashboard> children = new ArrayList<>();
    //@BsonIgnore
    private int level;
}
