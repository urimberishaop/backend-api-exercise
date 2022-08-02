package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static java.util.stream.Collectors.groupingBy;

@Singleton
public class DashboardCrudService {
    @Inject
    IMongoDB mongoDB;

    private static final String CONTENT_COLLECTION_NAME = "content";
    private final static String DASHBOARDS_COLLECTION_NAME = "dashboards";

    public CompletableFuture<List<Dashboard>> all(User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AtomicInteger skip = new AtomicInteger();
                int limit = 100;

                Bson accessFilter = Filters.or(
                        in("readACL", requestingUser.getId().toString()),
                        in("readACL", requestingUser.getRoles()),
                        in("writeACL", requestingUser.getId().toString()),
                        in("writeACL", requestingUser.getRoles()),
                        eq("readACL", new ArrayList<>()));

                long count = mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME)
                        .countDocuments(accessFilter);

                List<Dashboard> dashboards = new ArrayList<>();

                while (skip.get() < count) {
                    dashboards = mongoDB.getMongoDatabase()
                            .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                            .find(accessFilter)
                            .skip(skip.get())
                            .limit(limit)
                            .into(new ArrayList<>());

                    List<Content> contents = mongoDB.getMongoDatabase()
                            .getCollection(CONTENT_COLLECTION_NAME, Content.class)
                            .find(Filters.and(
                                    Filters.in("dashboardId", dashboards.stream().map(BaseModel::getId).collect(Collectors.toList())),
                                    accessFilter))
                            .into(new ArrayList<>());

                    dashboards.forEach(dashboard -> {
                        dashboard.setItems(contents.stream()
                                .filter(content -> content.getDashboardId().equals(dashboard.getId()))
                                .collect(Collectors.toList()));

                    skip.addAndGet(limit);
                    });
                }
                return dashboards;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .insertOne(dashboard);
                return dashboard;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Dashboard> update(Dashboard dashboard, String id, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                dashboard.setId(null);
                return mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .findOneAndReplace(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(
                                        eq("writeACL", requestingUser.getId().toString()),
                                        in("writeACL", requestingUser.getRoles()),
                                        eq("writeACL", new ArrayList<>()))), dashboard);
            } catch (CompletionException e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Dashboard not found."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong. " + e.getMessage()));
            }
        });
    }

    public CompletableFuture<Dashboard> delete(String id, User requestingUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .findOneAndDelete(Filters.and(
                                Filters.eq("_id", new ObjectId(id)),
                                Filters.or(
                                        Filters.eq("writeACL", requestingUser.getId().toString()),
                                        Filters.in("writeACL", requestingUser.getRoles()),
                                        Filters.eq("writeACL", new ArrayList<>()))));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found. Please re-check your input."));
            }
        });
    }

    public CompletableFuture<List<Dashboard>> hierarchy() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Dashboard> dashboards = mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .find()
                        .into(new ArrayList<>());

                List<Dashboard> parentlessCompanies = dashboards.stream()
                        .filter(x -> x.getParentId() == null)
                        .collect(Collectors.toList());

                return parentlessCompanies.stream()
                        .peek(x -> addChildren(x, dashboards))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }


    public void addChildren(Dashboard parent, List<Dashboard> list) {
        for (Dashboard x: list) {
            if (parent.getId().equals(x.getParentId())) {
                parent.getChildren().add(x);
                addChildren(x, list);
            }
        }
    }
}
