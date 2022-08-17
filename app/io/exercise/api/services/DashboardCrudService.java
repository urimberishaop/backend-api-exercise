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
import io.exercise.api.utils.ServiceUtils;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
public class DashboardCrudService {
	@Inject
	IMongoDB mongoDB;

	private static final String CONTENT_COLLECTION_NAME = "content";
	private final static String DASHBOARDS_COLLECTION_NAME = "dashboards";

	/**
	 * Returns the list of all dashboards (with content) the user is authorized to see
	 *
	 * @param requestingUser the user
	 * @param skip           skip for pagination
	 * @param limit          limit for pagination
	 * @return the list of all dashboards matching the criteria
	 */
	public CompletableFuture<List<Dashboard>> all(User requestingUser, int skip, int limit) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				List<Dashboard> dashboards = mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.find(ServiceUtils.readAccessFilter(requestingUser))
					.skip(skip)
					.limit(limit)
					.into(new ArrayList<>());

				List<Content> contents = mongoDB.getMongoDatabase()
					.getCollection(CONTENT_COLLECTION_NAME, Content.class)
					.find(Filters.and(
						Filters.in("dashboardId", dashboards.stream().map(BaseModel::getId).collect(Collectors.toList())),
						ServiceUtils.readAccessFilter(requestingUser)))
					.into(new ArrayList<>());

				dashboards.forEach(dashboard ->
					dashboard.setItems(
						contents.stream()
							.filter(content -> content.getDashboardId().equals(dashboard.getId()))
							.collect(Collectors.toList())));
				return dashboards;
			} catch (Exception e) {
				throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
			}
		});
	}

	/**
	 * Adds a dashboard to the Mongo collection
	 *
	 * @param dashboard the dashboard
	 * @return the dashboard that's been added
	 */
	public CompletableFuture<Dashboard> create(Dashboard dashboard) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.insertOne(dashboard);
				return dashboard;
			} catch (Exception e) {
				throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
			}
		});
	}

	/**
	 * Updates a dashboard in the Mongo collection
	 *
	 * @param dashboard the dashboard
	 * @return the dashboard that's been updated
	 */
	public CompletableFuture<Dashboard> update(Dashboard dashboard, String id, User requestingUser) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				dashboard.setId(null);
				mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.findOneAndReplace(
						Filters.and(
							Filters.eq("_id", new ObjectId(id)),
							ServiceUtils.writeAccessFilter(requestingUser)),
						dashboard);
				return dashboard;
			} catch (CompletionException e) {
				throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Dashboard not found."));
			} catch (Exception e) {
				throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
			}
		});
	}

	/**
	 * Deletes a dashboard from the Mongo collection
	 *
	 * @param id             the dashboard's ID
	 * @param requestingUser the User that's making a delete request
	 * @return the dashboard that's been deleted
	 */
	public CompletableFuture<Dashboard> delete(String id, User requestingUser) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				mongoDB.getMongoDatabase()
					.getCollection(CONTENT_COLLECTION_NAME, Content.class)
					.deleteMany(Filters.eq("dashboardId", new ObjectId(id)));

				mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.deleteMany(Filters.eq("parentId", new ObjectId(id)));

				return mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.findOneAndDelete(Filters.and(
						Filters.eq("_id", new ObjectId(id)),
						ServiceUtils.writeAccessFilter(requestingUser)));
			} catch (Exception e) {
				throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found."));
			}
		});
	}

	/**
	 * Returns the dashboards in a hierarchical manner together with their items
	 *
	 * @param requestingUser the user that's making the request
	 * @param skip           the pagination skip
	 * @param limit          the pagination limit
	 * @return the list of dashboards structured in a hierarchical manner
	 */
	public CompletableFuture<List<Dashboard>> hierarchy(User requestingUser, int skip, int limit) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				List<Bson> pipeline = Arrays.asList(
					Aggregates.match(ServiceUtils.readAccessFilter(requestingUser)),
					Aggregates.match(new Document("parentId", new BsonNull())),
					Aggregates.graphLookup(DASHBOARDS_COLLECTION_NAME, "$_id", "_id", "parentId", "children", new GraphLookupOptions().depthField("level")),
					Aggregates.skip(skip),
					Aggregates.limit(limit));

				List<Dashboard> parentlessDashboards = mongoDB.getMongoDatabase()
					.getCollection(DASHBOARDS_COLLECTION_NAME, Dashboard.class)
					.aggregate(pipeline, Dashboard.class)
					.into(new ArrayList<>());

				List<ObjectId> ids = parentlessDashboards.stream().map(BaseModel::getId).collect(Collectors.toList());
				parentlessDashboards.forEach(dash -> {
					ids.addAll(dash.getChildren().stream().map(BaseModel::getId).collect(Collectors.toList()));
				});

				List<Content> contents = mongoDB.getMongoDatabase()
					.getCollection(CONTENT_COLLECTION_NAME, Content.class)
					.find(Filters.in("dashboardId", ids))
					.into(new ArrayList<>());

				return parentlessDashboards.stream()
					.peek(dashboard -> {
						//Adding children
						List<Dashboard> children = dashboard.getChildren();
						children.stream()
							.filter(x -> x.getLevel() == 0)
							.forEach(x -> addChildren(x, children));
						dashboard.setChildren(children.stream().filter(x -> x.getLevel() == 0).collect(Collectors.toList()));

						//Adding content
						children.forEach(child -> child.setItems(
							contents.stream()
								.filter(content -> content.getDashboardId().equals(child.getId()))
								.collect(Collectors.toList())));

						dashboard.setItems(contents.stream()
							.filter(content -> content.getDashboardId().equals(dashboard.getId()))
							.collect(Collectors.toList()));

					}).collect(Collectors.toList());

			} catch (Exception e) {
				throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
			}
		});
	}

	/**
	 * The recursive method for adding children dashboards into their parents' children list
	 *
	 * @param parent the parent dashboard (should be one that has no parentId)
	 * @param list   the list of all dashboards
	 */
	public void addChildren(Dashboard parent, List<Dashboard> list) {
		for (Dashboard x : list) {
			if (parent.getId().equals(x.getParentId())) {
				parent.getChildren().add(x);
				addChildren(x, list);
			}
		}
	}
}
