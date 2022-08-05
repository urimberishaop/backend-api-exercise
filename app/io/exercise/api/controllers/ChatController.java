package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
//import java.util.concurrent.Flow;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ChatController extends Controller {

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;

    @Inject
    IMongoDB mongoDB;

    private static final String USERS_COLLECTION_NAME = "users";
    private static final String CHATROOMS_COLLECTION_NAME = "chatrooms";


    @SuppressWarnings("unchecked")
    public WebSocket chat(String roomId) {
        return WebSocket.Text.acceptOrResult(
                request -> CompletableFuture.completedFuture(
                        request.getHeaders()
                                .get("token")
                                .map(token -> {
                                    hasAccess(token, roomId);
                                    return F.Either.<Result, Flow<String, String, ?>>Right(
                                            ActorFlow.actorRef((out) ->
                                                    ChatActor.props(out, roomId), actorSystem, materializer));
                                })
                                .orElseGet(() -> {
                                    System.out.println("Forbidden.");
                                    return F.Either.Left(forbidden());
                                })));
    }

    public void hasAccess(String token, String roomId) {
        token = token.replace("bearer ", "");
        String tokenDecoded = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
        String userId = Json.parse(tokenDecoded).get("id").asText();

        User user = mongoDB.getMongoDatabase()
                .getCollection(USERS_COLLECTION_NAME, User.class)
                .find()
                .filter(Filters.eq("_id", new ObjectId(userId)))
                .first();

        if (user == null) {
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Authentication failed."));
        }

        ChatRoom cr = mongoDB.getMongoDatabase()
                .getCollection(USERS_COLLECTION_NAME, ChatRoom.class)
                .find()
                .filter(Filters.eq("roomId", roomId))
                .first();

        if (cr == null) {
            cr = new ChatRoom(roomId, List.of(user.getId().toString()));
            mongoDB.getMongoDatabase()
                    .getCollection(CHATROOMS_COLLECTION_NAME, ChatRoom.class)
                    .insertOne(cr);
        }

        if (!cr.getACL().contains(user.getId().toString())) {
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "You have no access to join this room."));
        }
    }
}
