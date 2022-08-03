package io.exercise.api.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.model.Filters;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
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
import java.util.concurrent.CompletionStage;

public class AuthenticationAction extends Action<Authentication> {

    @Inject
    IMongoDB mongoDB;

    private static final String USERS_COLLECTION_NAME = "users";

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(ConfigFactory.load().getString("play.http.secret.key"));

            if (request.header("token").isEmpty()) {
                return CompletableFuture.completedFuture(notFound("Token header not found."));
            }

            // Editing out the "bearer" part from "bearer {TOKEN}"
            String token = ServiceUtils.getTokenFromRequest(request)
                    .replace("bearer ", "");
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            String tokenDecoded = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
            JsonNode tokenJson = Json.parse(tokenDecoded);

            List<User> find = mongoDB.getMongoDatabase()
                    .getCollection(USERS_COLLECTION_NAME, User.class)
                    .find()
                    .filter(Filters.eq("_id", new ObjectId(tokenJson.get("id").asText())))
                    .into(new ArrayList<>());

            if (find.size() == 0) {
                return CompletableFuture.completedFuture(notFound("User not found. Please re-check your ID."));
            }

            request = request.addAttr(Attributes.USER_TYPED_KEY, find.get(0));
        } catch (SignatureVerificationException | JWTDecodeException e) {
            return CompletableFuture.completedFuture(badRequest("Incorrect token."));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(badRequest("Something went wrong. " + e));
        }
        return delegate.call(request);
    }
}
