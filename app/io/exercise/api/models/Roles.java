package io.exercise.api.models;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Roles {
    public static final Role Admin = new Role(new ObjectId("62e7dc19bcae4f4a49908dca"), "Admin");
    public static final Role Member = new Role(new ObjectId("62e90a69e08d17cd567438dd"), "Member");
    public static final Role PremiumMember = new Role(new ObjectId("62e90a944e9bf2171ca6df26"), "PremiumMember");
}