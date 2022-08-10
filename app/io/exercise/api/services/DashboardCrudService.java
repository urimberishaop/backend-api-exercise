package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.GraphLookupOptions;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.Content;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.*;
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
    private final static String SMALL_DASHBOARDS_COLLECTION_NAME = "dashboardsFinal";

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

                            /* THE RECURSIVE JAVA WAY */

//                List<Dashboard> dashboards = mongoDB.getMongoDatabase()
//                        .getCollection("dashboardsSmall", Dashboard.class)
//                        .find()
//                        .into(new ArrayList<>());
//
//                List<Dashboard> parentlessCompanies = dashboards.stream()
//                        .filter(x -> x.getParentId() == null)
//                        .collect(Collectors.toList());
//
//                return parentlessCompanies.stream()
//                        .peek(x -> addChildren(x, dashboards))
//                        .collect(Collectors.toList());


                            /* THE MONGO WAY */
                List<Bson> pipeline = Arrays.asList(new Document("$match",
                                new Document("parentId",
                                        new BsonNull())),
                        new Document("$graphLookup",
                                new Document("from", DASHBOARDS_COLLECTION_NAME)
                                        .append("startWith", "$_id")
                                        .append("connectFromField", "_id")
                                        .append("connectToField", "parentId")
                                        .append("as", "children")
                                        .append("depthField", "level")),
                        new Document("$project",
                                new Document("_id", 1L)
                                        .append("name", 1L)
                                        .append("description", 1L)
                                        .append("parentId", 1L)
                                        .append("readACL", 1L)
                                        .append("writeACL", 1L)
                                        .append("children._id", 1L)
                                        .append("children.name", 1L)
                                        .append("children.description", 1L)
                                        .append("children.parentId", 1L)
                                        .append("children.readACL", 1L)
                                        .append("children.writeACL", 1L)
                                        .append("children.level", 1L)),
                        new Document("$unwind", "$children"),
                        new Document("$sort",
                                new Document("children.level", -1L)),
                        new Document("$group",
                                new Document("_id", "$_id")
                                        .append("name",
                                                new Document("$first", "$name"))
                                        .append("description",
                                                new Document("$first", "$description"))
                                        .append("readACL",
                                                new Document("$first", "$readACL"))
                                        .append("writeACL",
                                                new Document("$first", "$writeACL"))
                                        .append("children",
                                                new Document("$push", "$children"))),
                        new Document("$addFields",
                                new Document("children",
                                        new Document("$reduce",
                                                new Document("input", "$children")
                                                        .append("initialValue",
                                                                new Document("currentLevel", -1L)
                                                                        .append("currentLevelChildren", Arrays.asList())
                                                                        .append("previousLevelChildren", Arrays.asList()))
                                                        .append("in",
                                                                new Document("$let",
                                                                        new Document("vars",
                                                                                new Document("prev",
                                                                                        new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$value.currentLevel", "$$this.level")), "$$value.previousLevelChildren", "$$value.currentLevelChildren")))
                                                                                        .append("current",
                                                                                                new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$value.currentLevel", "$$this.level")), "$$value.currentLevelChildren", Arrays.asList()))))
                                                                                .append("in",
                                                                                        new Document("currentLevel", "$$this.level")
                                                                                                .append("previousLevelChildren", "$$prev")
                                                                                                .append("currentLevelChildren",
                                                                                                        new Document("$concatArrays", Arrays.asList("$$current", Arrays.asList(new Document("$mergeObjects", Arrays.asList("$$this",
                                                                                                                new Document("children",
                                                                                                                        new Document("$filter",
                                                                                                                                new Document("input", "$$prev")
                                                                                                                                        .append("as", "e")
                                                                                                                                        .append("cond",
                                                                                                                                                new Document("$eq", Arrays.asList("$$e.parentId", "$$this._id"))))))))))))))))),
                        new Document("$addFields",
                                new Document("children", "$children.currentLevelChildren")));

                return mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .aggregate(pipeline, Dashboard.class)
                        .into(new ArrayList<>());


                        /* THE COMBINED MONGO JAVA WAY */

                /*int skip = 0, limit = 1;
                List<Bson> pipeline = Arrays.asList(new Document("$match",
                                new Document("parentId",
                                        new BsonNull())),
                        new Document("$graphLookup",
                        new Document("from", DASHBOARDS_COLLECTION_NAME)
                                .append("startWith", "$_id")
                                .append("connectFromField", "_id")
                                .append("connectToField", "parentId")
                                .append("as", "children")
                                .append("depthField", "level")),
                        Aggregates.skip(skip),
                        Aggregates.limit(limit));

                Dashboard d = mongoDB.getMongoDatabase()
                        .getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
                        .aggregate(pipeline, Dashboard.class)
                        .first();

                if (d == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "No dashboards found."));
                }

                Map<ObjectId, List<Dashboard>> map = d.getChildren()
                        .stream()
                        .collect(groupingBy(Dashboard::getParentId));

                map = map.entrySet().stream()
                        .sorted((x,y) -> y.getValue().get(0).getLevel() - x.getValue().get(0).getLevel())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

                map.forEach((key, value) -> {
                    Dashboard test = d.getChildren().stream().filter(child -> child.getId().equals(key)).findAny().orElse(null);
                    if (test != null) {
                        test.setChildren(value);
                        value.forEach(duplicate -> d.getChildren().remove(duplicate));
                    }
                });
                return List.of(d);*/

            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }


    public void addChildren(Dashboard parent, List<Dashboard> list) {
        for (Dashboard x : list) {
            if (parent.getId().equals(x.getParentId())) {
                parent.getChildren().add(x);
                addChildren(x, list);
            }
        }
    }
}
