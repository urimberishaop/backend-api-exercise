package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "EMAIL")
public class EmailContent extends Content{
    @NotEmpty
    String subject;
    @Pattern(message = "is not correct.", regexp = "/^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$/")
    String email;
    @NotEmpty
    String text;
    List<String> readACL;
    List<String> writeACL;

    @Override
    public Types getType() { return Types.EMAIL; }
}
