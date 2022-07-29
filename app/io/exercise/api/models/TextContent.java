package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "TEXT")
public class TextContent extends Content{
    @NotEmpty
    String text;
    List<String> readACL;
    List<String> writeACL;

    @Override
    public Types getType() { return Types.TEXT; }
}
