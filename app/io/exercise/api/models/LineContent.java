package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "Line")
public class LineContent extends Content{
    String url;
    List<CategoryValuePair> data = new ArrayList<>();

    @Override
    public Types getType() { return Types.Line; }
}
