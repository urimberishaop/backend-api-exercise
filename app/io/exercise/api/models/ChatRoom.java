package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseModel{
    @NotNull
    String roomId;
    @BsonProperty("readACL")
    List<String> readACL = new ArrayList<>();
    @BsonProperty("writeACL")
    List<String> writeACL = new ArrayList<>();
}
