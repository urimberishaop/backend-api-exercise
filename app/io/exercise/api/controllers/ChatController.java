package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import play.libs.F;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

public class ChatController extends Controller {

	@Inject
	private ActorSystem actorSystem;
	@Inject
	private Materializer materializer;

	@Inject
	IMongoDB mongoDB;

	@Inject
	Config config;

	public WebSocket chat(String roomId) {
		return WebSocket.Text.acceptOrResult(request -> {
			try {
				return ServiceUtils.getUserFromRequest(request, mongoDB, config)
					.thenCompose(user -> ServiceUtils.setUserAccessForRoom(user, roomId, mongoDB))
					.thenApply(user -> F.Either.Right(ActorFlow.actorRef((out) ->
						ChatActor.props(out, roomId, user), actorSystem, materializer)));
			} catch (Exception e) {
				F.Either<Result, Flow<String, String, ?>> left = F.Either.Left(status(500, "Something went wrong."));
				return CompletableFuture.completedFuture(left);
			}
		});
	}
}
