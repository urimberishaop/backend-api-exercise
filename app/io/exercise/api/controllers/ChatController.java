package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.model.Filters;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ChatController extends Controller {

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;

    @Inject
    IMongoDB mongoDB;

    private static final String USERS_COLLECTION_NAME = "users";
    private static final String CHATROOMS_COLLECTION_NAME = "chatrooms";


    public WebSocket chat(String roomId) {
        return WebSocket.Text.acceptOrResult(request -> {
            try {
                User user = getUserFromRequest(request);
                ChatRoom cr = getRoomById(roomId, user);
                boolean userHasWriteAccess = getUserAccess(user, cr).get(1);

                return CompletableFuture.completedFuture(F.Either.Right(
                        ActorFlow.actorRef((out) ->
                                ChatActor.props(out, roomId, userHasWriteAccess, user), actorSystem, materializer)));
            } catch (UnsupportedEncodingException e) {
                return getResponse(400, "Incorrect token.");
            } catch (Exception e) {
                return getResponse(500, "Something went wrong.");
            }
        });
    }

    public List<Boolean> getUserAccess(User user, ChatRoom cr) {
        List<Boolean> result = Arrays.asList(false, false);
        user.getRoles().add(user.getId().toString());

        for (String x : user.getRoles()) {
            if (cr.getReadACL().contains(x)) {
                result.set(0, true);
            }
            if (cr.getWriteACL().contains(x)) {
                result.set(1, true);
            }
        }
        if (!result.get(0) && !result.get(1)) {
            throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, "You have no access to join this room."));
        }
        return result;
    }

    public User getUserFromRequest(Http.RequestHeader request) throws UnsupportedEncodingException {
        String token = request.getHeaders().get("token").get();

        if (request.getHeaders().get("token").isEmpty()) {
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Token header missing."));
        }

        token = token.replace("bearer ", "");
        String tokenDecoded = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
        String userId = Json.parse(tokenDecoded).get("id").asText();

        Algorithm algorithm = Algorithm.HMAC256(ConfigFactory.load().getString("play.http.secret.key"));
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
    }

    public ChatRoom getRoomById(String roomId, User user) {
        ChatRoom cr = mongoDB.getMongoDatabase()
                .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                .find()
                .filter(Filters.eq("roomId", roomId))
                .first();

        if (cr == null) {
            cr = new ChatRoom(roomId, List.of(user.getId().toString()), List.of(user.getId().toString()));
            mongoDB.getMongoDatabase()
                    .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                    .insertOne(cr);
        }
        return cr;
    }

    public CompletableFuture<F.Either<Result, Flow<String, String, ?>>> getResponse(int code, String text) {
        F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(status(code, text));
        return CompletableFuture.completedFuture(left);
    }
}
