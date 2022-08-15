package io.exercise.api.actions;

import com.typesafe.config.Config;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Authenticates a user and adds it to the request.
 */
public class AuthenticationAction extends Action<Authentication> {

	@Inject
	IMongoDB mongoDB;

	@Inject
	Config config;

	@Override
	public CompletionStage<Result> call(Http.Request request) {
		try {
			User user = ServiceUtils.getUserFromRequest(request, mongoDB, config).join();
			return delegate.call(request.addAttr(Attributes.USER_TYPED_KEY, user));
		} catch (CompletionException e) {
			return CompletableFuture.completedFuture(badRequest(e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			return CompletableFuture.completedFuture(badRequest(e.toString()));
		}
	}
}
