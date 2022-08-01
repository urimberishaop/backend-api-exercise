package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "Line")
public class LineContent extends Content {
    @NotEmpty(message = "cannot be empty!")
    List<CategoryValuePair> data = new ArrayList<>();

    @Override
    public Types getType() {
        return Types.Line;
    }
}
