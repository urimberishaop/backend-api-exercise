package io.exercise.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.exercise.api.models.Dashboard;
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
            Dashboard user = Json.fromJson(request.body().asJson(), Dashboard.class);
            mongoDB.getMongoDatabase()
                    .getCollection("users", Dashboard.class)
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

        Dashboard user = Json.fromJson(request.body().asJson(), Dashboard.class);
        Dashboard isFound = mongoDB.getMongoDatabase()
                .getCollection("users", Dashboard.class)
                .findOneAndReplace(eq("_id", id), user);

        if (isFound == null) {
            return notFound("User not found.");
        }

        return ok(Json.toJson(user));
    }


    public Result delete(String id) {
        Dashboard user =  mongoDB.getMongoDatabase()
                .getCollection("users", Dashboard.class)
                .findOneAndDelete(eq("_id", id));

        if (user == null) {
            return notFound("User not found.");
        }

        return ok(Json.toJson(user));
    }





}
