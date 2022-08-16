package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseModel {
	@NotNull(message = "cannot be empty!")
	@Size(min = 5, max = 20)
	private String username;
	@NotNull(message = "cannot be empty!")
	@Size(min = 8, max = 50)
	private String password;
	private List<String> roles = new ArrayList<>();
	@JsonIgnore
	@BsonIgnore
	boolean readAccess = false;
	@JsonIgnore
	@BsonIgnore
	boolean writeAccess = false;
}
