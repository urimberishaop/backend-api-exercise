package io.exercise.api.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.exercise.api.models.validators.HibernateValidator;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Validates an object with HibernateValidator.
 */
public class ValidationAction extends Action<Validation> {

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            JsonNode body = request.body().asJson();
            Object object = Json.fromJson(body, configuration.type());

            String errors = HibernateValidator.validate(object);
            if (!Strings.isNullOrEmpty(errors)) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(errors)));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(badRequest(e.toString()));
        }
        return delegate.call(request);
    }
}
