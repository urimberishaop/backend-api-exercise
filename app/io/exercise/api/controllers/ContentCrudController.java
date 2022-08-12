package io.exercise.api.controllers;

import io.exercise.api.actions.Authentication;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.Content;
import io.exercise.api.services.ContentCrudService;
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
 * for the Content CRUD operations.
 */
@Authentication
public class ContentCrudController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    ContentCrudService service;

    /**
     * Adds content from request body
     * @param request the request containing the content
     * @return the content that's added (as Json)
     */
    @Validation
    public CompletableFuture<Result> create(Http.Request request) {
        return serializationService.parseListBodyOfType(request, Content.class)
                .thenCompose((data) -> service.create(data, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Returns a list of content (in a dashboard) based on User's access (via token)
     * @param request the request
     * @param id ID of the dashboard
     * @param skip the pagination skip
     * @param limit the pagination limit
     * @return a Json containing the list of content
     */
    public CompletableFuture<Result> all(Http.Request request, String id, int skip, int limit) {
        return service.all(ServiceUtils.getUserFrom(request), id, skip, limit)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Updates a content record by ID.
     * @param request the request of the updated content
     * @param id the ID of content that's going to be updated
     * @return the content that's been updated
     */
    @Validation
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.update(data, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Deletes a content record by ID.
     * @param request the request (we need it for the user)
     * @param id the ID of the content that's going to be deleted
     * @return the content that's been deleted
     */
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return service.delete(id, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
