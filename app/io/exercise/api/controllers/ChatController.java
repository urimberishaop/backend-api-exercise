package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.mongodb.client.model.Filters;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            if (request.getHeaders().get("token").isEmpty()) {
                final F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(Results.badRequest("Authorization token missing."));
                return CompletableFuture.completedFuture(left);
            }

            User user = getUserFromToken(request.getHeaders().get("token").get());

            if (user == null) {
                final F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(Results.notFound("User not found."));
                return CompletableFuture.completedFuture(left);
            }

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

            boolean userHasReadAccess = false, userHasWriteAccess = false;
            user.getRoles().add(user.getId().toString());
            for (String x: user.getRoles()) {
                if (cr.getReadACL().contains(x)) {
                    userHasReadAccess = true;
                }
                if (cr.getWriteACL().contains(x)) {
                    userHasWriteAccess = true;
                }
            }


            if (!userHasReadAccess && !userHasWriteAccess) {
                final F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(Results.forbidden("You have no access to join this room."));
                return CompletableFuture.completedFuture(left);
            }

            boolean finalUserHasWriteAccess = userHasWriteAccess;
            return CompletableFuture.completedFuture(F.Either.Right(
                    ActorFlow.actorRef((out) ->
                            ChatActor.props(out, roomId, finalUserHasWriteAccess, user), actorSystem, materializer)));
        });

    }

    public User getUserFromToken(String token) {
        token = token.replace("bearer ", "");
        String tokenDecoded = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
        String userId = Json.parse(tokenDecoded).get("id").asText();

        return mongoDB.getMongoDatabase()
                .getCollection(USERS_COLLECTION_NAME, User.class)
                .find()
                .filter(Filters.eq("_id", new ObjectId(userId)))
                .first();
    }
}
