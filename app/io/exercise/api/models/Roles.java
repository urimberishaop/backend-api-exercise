package io.exercise.api.models;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Roles {
    public static final Role Admin = new Role(new ObjectId("62e7dc19bcae4f4a49908dca"), "Admin");
}