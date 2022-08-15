package io.exercise.api.controllers;

import io.exercise.api.actions.Authentication;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.Dashboard;
import io.exercise.api.services.DashboardCrudService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This controller contains actions to handle HTTP requests
 * for the Dashboard CRUD operations.
 */
@Authentication
public class DashboardCrudController extends Controller {

	@Inject
	SerializationService serializationService;

	@Inject
	DashboardCrudService service;

	/**
	 * Adds a dashboard from request body
	 *
	 * @param request the request containing the dashboard
	 * @return the dashboard that's added (as Json)
	 */
	@Validation
	public CompletableFuture<Result> create(Http.Request request) {
		return serializationService.parseBodyOfType(request, Dashboard.class)
			.thenCompose((data) -> service.create(data))
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	/**
	 * Returns a list of dashboards based on User's access (via token)
	 *
	 * @param request the request
	 * @param skip    the pagination skip
	 * @param limit   the pagination limit
	 * @return a Json containing the list of dashboards
	 */
	public CompletableFuture<Result> all(Http.Request request, int skip, int limit) {
		return service.all(ServiceUtils.getUserFrom(request), skip, limit)
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	/**
	 * Updates a dashboard by ID.
	 *
	 * @param request the request of the updated dashboard
	 * @param id      the ID of dashboard that's going to be updated
	 * @return the dashboard that's been updated
	 */
	@Validation
	public CompletableFuture<Result> update(Http.Request request, String id) {
		return serializationService.parseBodyOfType(request, Dashboard.class)
			.thenCompose((data) -> service.update(data, id, ServiceUtils.getUserFrom(request)))
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	/**
	 * Deletes a dashboard record by ID.
	 *
	 * @param request the request (we need it for the user)
	 * @param id      the ID of the dashboard that's going to be deleted
	 * @return the dashboard that's been deleted
	 */
	public CompletableFuture<Result> delete(Http.Request request, String id) {
		return service.delete(id, ServiceUtils.getUserFrom(request))
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	/**
	 * Returns a list of dashboards based on User's access (via token) BUT in a hierarchical structure
	 *
	 * @param request the request
	 * @param skip    the pagination skip
	 * @param limit   the pagination limit
	 * @return the list of dashboards structured in a hierarchical manner
	 */
	public CompletableFuture<Result> hierarchy(Http.Request request, int skip, int limit) {
		return service.hierarchy(ServiceUtils.getUserFrom(request), skip, limit)
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

}