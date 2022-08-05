package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseModel{
    String roomId;
    @BsonProperty("ACL")
    List<String> ACL = new ArrayList<>();
}
