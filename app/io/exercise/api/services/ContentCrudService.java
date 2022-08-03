package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class ContentCrudService {
    @Inject
    IMongoDB mongoDB;

    private static final String CONTENT_COLLECTION_NAME = "content";
    private static final String USERS_COLLECTION_NAME = "users";
    private static final String DASHBOARDS_COLLECTION_NAME = "dashboards";

    public CompletableFuture<List<Content>> all(User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .find(Filters.or(
                                Filters.eq("readACL", requestingUser.getId().toString()),
                                Filters.in("readACL", requestingUser.getRoles()),
                                Filters.eq("writeACL", requestingUser.getId().toString()),
                                Filters.in("writeACL", requestingUser.getRoles()),
                                Filters.eq("readACL", new ArrayList<>())))
                        .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<List<Content>> create(List<Content> contents, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                contents.forEach(content -> {
                    Dashboard requiredDashboard = mongoDB.getMongoDatabase()
                            .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                            .find(Filters.and(
                                    Filters.eq("_id", content.getDashboardId()),
                                    Filters.or(
                                            Filters.eq("writeACL", requestingUser.getId().toString()),
                                            Filters.in("writeACL", requestingUser.getRoles()),
                                            Filters.eq("writeACL", new ArrayList<>()))))
                            .first();

                    if (requiredDashboard == null) {
                        return;
                    }

                    mongoDB.getMongoDatabase()
                            .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                            .insertOne(content);
                });
                return contents;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<Content> update(Content content, String id, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                content.setId(null);
                return mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .findOneAndReplace(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(
                                        Filters.eq("writeACL", requestingUser.getId().toString()),
                                        Filters.in("writeACL", requestingUser.getRoles()),
                                        Filters.eq("writeACL", new ArrayList<>()))), content);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<Content> delete(String id, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .findOneAndDelete(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(
                                        Filters.eq("writeACL", requestingUser.getId().toString()),
                                        Filters.in("writeACL", requestingUser.getRoles()),
                                        Filters.eq("writeACL", new ArrayList<>()))));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }
}
