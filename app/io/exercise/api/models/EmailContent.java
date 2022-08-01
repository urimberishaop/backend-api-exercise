package io.exercise.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "EMAIL")
public class EmailContent extends Content{
    @Size(min = 10, max = 200, message = "must be between 10 and 200 characters")
    String subject;
    //@Pattern(message = "is not correct.", regexp = "/^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$/")
    @Email
    String email;
    @NotEmpty(message = "cannot be empty!")
    String text;

    @Override
    public Types getType() { return Types.EMAIL; }
}
