package io.exercise.api.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.models.*;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.ContentCrudService;
import io.exercise.api.services.DashboardCrudService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ContentCrudController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    ContentCrudService service;

    public CompletableFuture<Result> create(Http.Request request) {
        return serializationService.parseListBodyOfType(request, Content.class)
                .thenCompose((data) -> service.create(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> all(String id) {
        return service.all(id)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.update(data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> delete(String id) {
        return service.delete(id)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
