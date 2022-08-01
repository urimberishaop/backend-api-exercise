package io.exercise.api.controllers;

import io.exercise.api.models.User;
import io.exercise.api.services.AuthenticationService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class AuthenticationController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    AuthenticationService service;

    public CompletableFuture<Result> authenticate(Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.authenticate(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
