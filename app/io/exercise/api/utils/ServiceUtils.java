package io.exercise.api.utils;

import akka.stream.javadsl.Flow;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.actions.Attributes;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static play.mvc.Results.status;

public class ServiceUtils {

    private static final String USERS_COLLECTION_NAME = "users";
    private static final String CHATROOMS_COLLECTION_NAME = "chatrooms";

    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    public static String getTokenFromRequest(Http.Request request) {
        return request.getHeaders().get("token").orElse(null);
    }

    public static Http.Request addToRequest(Http.Request request, User user) {
        return request.addAttr(Attributes.USER_TYPED_KEY, user);
    }

    public static CompletableFuture<User> setUserAccessForRoom(User user, String roomId, IMongoDB mongoDB) {
        return CompletableFuture.supplyAsync(() -> {
            user.getRoles().add(user.getId().toString());

            ChatRoom cr = mongoDB.getMongoDatabase()
                    .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                    .find(Filters.eq("roomId", roomId))
                    .first();

            if (cr == null) {
                cr = new ChatRoom(roomId, List.of(user.getId().toString()), List.of(user.getId().toString()));
                mongoDB.getMongoDatabase()
                        .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                        .insertOne(cr);
            }

            for (String x : user.getRoles()) {
                if (cr.getReadACL().contains(x)) {
                    user.setReadAccess(true);
                }
                if (cr.getWriteACL().contains(x)) {
                    user.setWriteAccess(true);
                }
            }
            if (!user.isReadAccess() && !user.isWriteAccess()) {
                throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, "You have no access to join this room."));
            }
            return user;
        });
    }

    public static CompletableFuture<User> getUserFromRequest(Http.RequestHeader request, IMongoDB mongoDB, Config config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = request.getHeaders().get("token").get();

                if (request.getHeaders().get("token").isEmpty()) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Token header missing."));
                }

                token = token.replace("bearer ", "");
                String tokenDecoded = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
                String userId = Json.parse(tokenDecoded).get("id").asText();

                Algorithm algorithm = Algorithm.HMAC256(config.getString("play.http.secret.key"));
                JWTVerifier verifier = JWT.require(algorithm)
                        .build();
                DecodedJWT jwt = verifier.verify(token);

                User user = mongoDB.getMongoDatabase()
                        .getCollection(USERS_COLLECTION_NAME, User.class)
                        .find()
                        .filter(Filters.eq("_id", new ObjectId(userId)))
                        .first();

                if (user == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found."));
                }

                return user;
            } catch (NoSuchElementException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Token header missing."));
            } catch (UnsupportedEncodingException | SignatureVerificationException e) {
                throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, "Incorrect token."));
            } catch (Exception e) {
                e.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public static CompletableFuture<ChatRoom> getRoomById(String roomId, IMongoDB mongoDB) {
        return CompletableFuture.supplyAsync(() -> {
            ChatRoom cr = mongoDB.getMongoDatabase()
                    .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                    .find()
                    .filter(Filters.eq("roomId", roomId))
                    .first();
            if (cr == null) {
                cr = new ChatRoom(roomId, new ArrayList<>(), new ArrayList<>());
                mongoDB.getMongoDatabase()
                        .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                        .insertOne(cr);
            }
            return cr;
        });
    }

    public static CompletableFuture<F.Either<Result, Flow<String, String, ?>>> getResponse(int code, String text) {
        F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(status(code, text));
        return CompletableFuture.completedFuture(left);
    }

    public static Result throwableToResult (Throwable error) {
        Result status = statusFromThrowable(error);
        if (status != null) {
            return status;
        }
        ObjectNode result = Json.newObject();
        result.put("status", 501);
        result.put("message", error.getLocalizedMessage());
        return status(501, result);
    }

    public static Result statusFromThrowable (Throwable error) {
        if (error instanceof RequestException) {
            return statusFromThrowable((RequestException) error);
        }
        if (error.getCause() == null) {
            return null;
        }
        return statusFromThrowable(error.getCause());
    }
}
