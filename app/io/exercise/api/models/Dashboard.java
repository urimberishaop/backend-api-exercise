package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
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
    @BsonIgnore
    private List<Content> items = new ArrayList<>();
    @BsonIgnore
    @BsonProperty("children")
    private List<Dashboard> children = new ArrayList<>();
}
