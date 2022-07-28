package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "IMAGE")
public class ImageContent extends Content{
    String url;

    @Override
    public Types getType() { return Types.IMAGE; }
}
