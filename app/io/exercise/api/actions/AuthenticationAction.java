package io.exercise.api.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class AuthenticationAction extends Action<Authentication> {

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            User user = ServiceUtils.getUserFromRequest(request, mongoDB, config).join();
            request.addAttr(Attributes.USER_TYPED_KEY, user);
        } catch (CompletionException e) {
            return CompletableFuture.completedFuture(badRequest(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(badRequest(e.toString()));
        }
        return delegate.call(request);
    }
}
