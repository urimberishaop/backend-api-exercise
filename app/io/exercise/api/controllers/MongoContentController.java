package io.exercise.api.controllers;

import com.mongodb.client.model.Filters;
import io.exercise.api.models.*;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.DatabaseUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class MongoContentController extends Controller {

    @Inject
    IMongoDB mongoDB;

    private final String collectionName = "users";

    public Result create(Http.Request request) {
        try {
            List<Content> contents = DatabaseUtils.parseJsonListOfType(request.body().asJson(), Content.class);

            contents.forEach(content -> {
                List<Dashboard> requiredDashboardList = mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .find(Filters.eq("_id", content.getDashboardId()))
                        .into(new ArrayList<>());

                if (requiredDashboardList.size() == 0) {
                    return;// notFound("Dashboard not found");
                }

                Dashboard requiredDashboard = requiredDashboardList.get(0);

                if (content instanceof ImageContent) {
                    ImageContent imageContent = (ImageContent) content;
                    requiredDashboard.getContent().add(imageContent);
                    System.out.println("Type of content is image");
                } else if (content instanceof TextContent) {
                    TextContent textContent = (TextContent) content;
                    requiredDashboard.getContent().add(textContent);
                    System.out.println("Type of content is text");
                } else if (content instanceof EmailContent) {
                    EmailContent emailContent = (EmailContent) content;
                    requiredDashboard.getContent().add(emailContent);
                    System.out.println("Type of content is email");
                } else if (content instanceof LineContent) {
                    LineContent lineContent = (LineContent) content;
                    requiredDashboard.getContent().add(lineContent);
                    System.out.println("Type of content is line");
                } else {
                    requiredDashboard.getContent().add(content);
                    System.out.println("Type of content is content");
                }

                mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .findOneAndReplace(Filters.eq("_id", content.getDashboardId()), requiredDashboard);

            });

            return ok(Json.toJson(contents));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest("Cannot create Dashboard content from your input. " + e);
        }
    }

    public Result all(String id) {
        List<Dashboard> requiredDashboard = mongoDB.getMongoDatabase()
                .getCollection(collectionName, Dashboard.class)
                .find(Filters.eq("_id", new ObjectId(id)))
                .into(new ArrayList<>());

        if (requiredDashboard.size() == 0) {
            return notFound(Json.toJson("Dashboard not found."));
        }

        List<Content> contents = requiredDashboard.get(0).getContent();
        return ok(Json.toJson(contents));
    }

    public Result update(Http.Request request, String id) {

        //TODO: finish the update method
        List<Dashboard> requiredDashboard = mongoDB.getMongoDatabase()
                .getCollection(collectionName, Dashboard.class)
                .find(Filters.eq("content.dashboardId", new ObjectId(id)))
                .into(new ArrayList<>());

        return ok(Json.toJson(requiredDashboard));
    }
//
//
//    public Result delete(String id) {
//        Dashboard user =  mongoDB.getMongoDatabase()
//                .getCollection("users", Dashboard.class)
//                .findOneAndDelete(eq("_id", id));
//
//        if (user == null) {
//            return notFound("User not found.");
//        }
//
//        return ok(Json.toJson(user));
//    }





}
