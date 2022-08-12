package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
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
    private static final String DASHBOARDS_COLLECTION_NAME = "dashboards";

    /**
     * Returns a list of all the content in a dashboard
     * @param requestingUser the user
     * @return the list of all content the user can see
     */
    public CompletableFuture<List<Content>> all(User requestingUser, String id, int skip, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                        .find(Filters.and(
                                ServiceUtils.readAccessFilter(requestingUser),
                                Filters.eq("dashboardId", new ObjectId(id))))
                        .skip(skip)
                        .limit(limit)
                        .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    /**
     * Adds content to the Mongo collection
     * @param contents the list of content that's going to be added
     * @param requestingUser the user that's going to add
     * @return a list of the content that's been added
     */
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
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Dashboard not found for this content."));
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

    /**
     * Updates a content record in the Mongo collection
     * @param content the updated content
     * @param id the ID of the content that's going to be updated
     * @param requestingUser the user that's trying to update content
     * @return the updated content
     */
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

    /**
     * Deletes a content record from the Mongo collection
     * @param id the content's ID
     * @param requestingUser the User that's making a delete request
     * @return the content record that's been deleted
     */
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
