package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "IMAGE")
public class ImageContent extends Content {
    @NotEmpty(message = "cannot be empty!")
    String url;

    @Override
    public Types getType() {
        return Types.IMAGE;
    }
}
