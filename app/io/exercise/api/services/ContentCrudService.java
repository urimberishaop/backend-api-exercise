package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class ContentCrudService {
    @Inject
    IMongoDB mongoDB;

    final String collectionName = "content";

    public CompletableFuture<List<Content>> all(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Content.class)
                        .find(Filters.eq("dashboardId", new ObjectId(id)))
                        .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<List<Content>> create(List<Content> contents) {
        return CompletableFuture.supplyAsync(() -> {
            try {
            mongoDB.getMongoDatabase()
                    .getCollection(collectionName, Content.class)
                    .insertMany(contents);

            return contents;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Content> update(Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                if (content.getDashboardId() == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Dashboard ID cannot be empty."));
                }

                if (!ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
                }
                mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Content.class)
                        .findOneAndReplace(eq("_id", new ObjectId(id)), content);
                return content;
            } catch (CompletionException e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Content> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                if (!ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
                }
                return mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Content.class)
                        .findOneAndDelete(eq("_id", new ObjectId(id)));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found. Please re-check your input."));
            }
        });
    }
}
