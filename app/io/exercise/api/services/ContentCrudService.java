package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
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

    private static final String CONTENT_COLLECTION_NAME = "content";
    private static final String USERS_COLLECTION_NAME = "authentication";
    private static final String DASHBOARDS_COLLECTION_NAME = "dashboards";

    public CompletableFuture<List<Content>> all(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .find(Filters.eq("dashboardId", new ObjectId(id)))
                        .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<List<Content>> create(List<Content> contents, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                contents.forEach(content -> {
                    // Checking for user IDs & user roles
                    if (!content.getWriteACL().contains(requestingUser.getId().toString()) &&
                            requestingUser.getRoles().stream().filter(x -> content.getWriteACL().contains(x.getId().toString())).findAny().isEmpty()) {
                        throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, "You have no access to add to this dashboard."));
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

    public CompletableFuture<Content> update(Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (content.getDashboardId() == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Dashboard ID cannot be empty."));
                }

                if (!ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
                }
                content.setId(null);
                mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .findOneAndReplace(eq("_id", new ObjectId(id)), content);
                return content;
            } catch (CompletionException e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
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
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .findOneAndDelete(eq("_id", new ObjectId(id)));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Content not found. Please re-check your input."));
            }
        });
    }
}
