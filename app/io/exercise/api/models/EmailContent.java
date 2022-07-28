package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "EMAIL")
public class EmailContent extends Content{
    String subject;
    String email;
    String text;
    List<String> readACL;
    List<String> writeACL;

    @Override
    public Types getType() { return Types.EMAIL; }
}
