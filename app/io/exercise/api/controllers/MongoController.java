package io.exercise.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class MongoController extends Controller {

    @Inject
    IMongoDB mongoDB;

    public Result create(Http.Request request) {
        try {
            User user = Json.fromJson(request.body().asJson(), User.class);
            mongoDB.getMongoDatabase()
                    .getCollection("users", User.class)
                    .insertOne(user);
            return ok(Json.toJson(user));
        } catch (Exception e) {
            return badRequest("Cannot create a user from your input.");
        }
    }

    public Result all() {


        MongoCollection<Document> collection = mongoDB.getMongoDatabase()
                .getCollection("users");
        List<Document> result = collection.find().into(new ArrayList<>());

        return ok(Json.toJson(result));
    }

    public Result update(Http.Request request, String id) {

        User user = Json.fromJson(request.body().asJson(), User.class);
        User isFound = mongoDB.getMongoDatabase()
                .getCollection("users", User.class)
                .findOneAndReplace(eq("_id", id), user);

        if (isFound == null) {
            return notFound("User not found.");
        }

        return ok(Json.toJson(user));
    }


    public Result delete(String id) {
        User user =  mongoDB.getMongoDatabase()
                .getCollection("users", User.class)
                .findOneAndDelete(eq("_id", id));

        if (user == null) {
            return notFound("User not found.");
        }

        return ok(Json.toJson(user));
    }





}
