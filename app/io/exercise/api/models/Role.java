package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role {
    private ObjectId id;
    private String name;

    public Role(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }
}