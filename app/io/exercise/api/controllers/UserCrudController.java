package io.exercise.api.controllers;

import io.exercise.api.actions.Validation;
import io.exercise.api.models.User;
import io.exercise.api.services.SerializationService;
import io.exercise.api.services.UserCrudService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class UserCrudController extends Controller {

	@Inject
	SerializationService serializationService;

	@Inject
	UserCrudService service;

	@Validation(type = User.class)
	public CompletableFuture<Result> create(Http.Request request) {
		return serializationService.parseBodyOfType(request, User.class)
			.thenCompose((data) -> service.create(data))
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	public CompletableFuture<Result> all(Http.Request request) {
		return service.all()
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	@Validation(type = User.class)
	public CompletableFuture<Result> update(Http.Request request, String id) {
		return serializationService.parseBodyOfType(request, User.class)
			.thenCompose((data) -> service.update(data, id))
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}

	public CompletableFuture<Result> delete(Http.Request request, String id) {
		return service.delete(id)
			.thenCompose((data) -> serializationService.toJsonNode(data))
			.thenApply(Results::ok)
			.exceptionally(DatabaseUtils::throwableToResult);
	}
}