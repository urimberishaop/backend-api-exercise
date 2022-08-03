package io.exercise.api.controllers;

import com.mongodb.client.model.Filters;
import io.exercise.api.actions.Authentication;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.DashboardCrudService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
@Authentication
public class DashboardCrudController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardCrudService service;
    /*
    @Inject
    IMongoDB mongoDB;

    public Result addContent() throws FileNotFoundException {

        List<Content> contents = mongoDB.getMongoDatabase()
                .getCollection("content", Content.class)
                .find(Filters.eq("type", "IMAGE"))
                .into(new ArrayList<>());

        Scanner s = new Scanner(new File("C:\\Users\\prime\\Desktop\\dashboardIds.txt"));
        ArrayList<String> list = new ArrayList<String>();
        while (s.hasNext()){
            list.add(s.next());
        }
        s.close();

        List<String> ids = Arrays.asList("62e257f6dbff353f56ef3024", "62e257f6dbff353f56ef3025", "62e90a944e9bf2171ca6df26", "62e90a69e08d17cd567438dd", "62e7dc19bcae4f4a49908dca", "62ea2bab12d9a44c9e2c1485", "62ea2bb52198b55274b88afa",
                "62ea2bb98f2b3cbbd9d853de", "62ea2bbc21982230a035bdc0", "62ea2bc0b826c8c660a552e6");

        contents.forEach(x -> {

            Random rand = new Random();
            String randomElement = list.get(rand.nextInt(list.size()));
            x.setDashboardId(new ObjectId(randomElement));

            String randomElement2 = ids.get(rand.nextInt(ids.size()));
            String randomElement3 = ids.get(rand.nextInt(ids.size()));
            if (!randomElement2.equals(randomElement3))
                x.setReadACL(Arrays.asList(randomElement2, randomElement3));
            else
                x.setReadACL(Arrays.asList(randomElement2));

            String randomElement4 = ids.get(rand.nextInt(ids.size()));
            String randomElement5 = ids.get(rand.nextInt(ids.size()));

            if (!randomElement4.equals(randomElement5))
                x.setWriteACL(Arrays.asList(randomElement4, randomElement5));
            else
                x.setWriteACL(Arrays.asList(randomElement4));

        });

        mongoDB.getMongoDatabase()
                .getCollection("content", Content.class)
                .drop();

        mongoDB.getMongoDatabase()
                .getCollection("content", Content.class)
                .insertMany(contents);


        return ok(Json.toJson(contents.size()));
    }

    */
    @Validation
    public CompletableFuture<Result> create(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((data) -> service.create(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> all(Http.Request request) {
        return service.all(ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((data) -> service.update(data, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return service.delete(id, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> hierarchy(Http.Request request) {
        return service.hierarchy()
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

}