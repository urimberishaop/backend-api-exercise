package io.exercise.api.models;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Roles {
    public static final Role Admin = new Role(new ObjectId("62e7dc19bcae4f4a49908dca"), "Admin");
    public static final Role Member = new Role(new ObjectId("62e90a69e08d17cd567438dd"), "Member");
    public static final Role PremiumMember = new Role(new ObjectId("62e90a944e9bf2171ca6df26"), "PremiumMember");

    //IDs that can be used 62ea2bab12d9a44c9e2c1485 62ea2bb52198b55274b88afa 62ea2bb98f2b3cbbd9d853de 62ea2bbc21982230a035bdc0 62ea2bc0b826c8c660a552e6
}