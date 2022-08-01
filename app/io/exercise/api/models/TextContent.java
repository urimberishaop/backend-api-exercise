package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "TEXT")
public class TextContent extends Content {
    @Size(min = 10, max = 200, message = "must be between 10 and 200 characters")
    String text;

    @Override
    public Types getType() {
        return Types.TEXT;
    }
}
