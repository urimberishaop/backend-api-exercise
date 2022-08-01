package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import io.exercise.api.exceptions.RequestException;
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
public class DashboardCrudService {
    @Inject
    IMongoDB mongoDB;

    final String collectionName = "dashboards";

    public CompletableFuture<List<Dashboard>> all() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .find()
                        .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .insertOne(dashboard);
                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Dashboard> update(Dashboard dashboard, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Dashboard not found. Please re-check your input."));
                }
                dashboard.setId(null);
                mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .findOneAndReplace(eq("_id", new ObjectId(id)), dashboard);
                return dashboard;
            } catch (CompletionException e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Dashboard not found. Please re-check your input."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Dashboard> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found. Please re-check your input."));
                }
                return mongoDB.getMongoDatabase()
                        .getCollection(collectionName, Dashboard.class)
                        .findOneAndDelete(new BasicDBObject("_id", new ObjectId(id)));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found. Please re-check your input."));
            }
        });
    }
}
