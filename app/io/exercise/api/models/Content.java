package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImageContent.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = TextContent.class, name = "TEXT"),
        @JsonSubTypes.Type(value = EmailContent.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = LineContent.class, name = "Line")
})
@BsonDiscriminator(key = "type", value = "NONE")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Content extends BaseModel {
    ObjectId id;
    @NotEmpty(message = "cannot be empty!")
    ObjectId dashboardId;
    @NotEmpty(message = "cannot be empty!")
    Types type = Types.NONE;
    List<String> readACL = new ArrayList<>();
    List<String> writeACL = new ArrayList<>();
}