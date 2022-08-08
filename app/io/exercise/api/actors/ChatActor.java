package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;

import javax.inject.Inject;

import static com.mongodb.client.model.Filters.eq;

/**
 * Chat Actor - Representing a user in a room!
 */
public class ChatActor extends AbstractActor {
	/**
	 * For logging purposes
	 */
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	/**
	 * String messages as constants
	 */
	private static final String JOINED_ROOM = "has joined the Room!";
	private static final String LEFT_ROOM = "has left the Room!";
	private static final String PING = "PING";
	private static final String PONG = "PONG";

	/**
	 * Mediator
	 */
	private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
	/**
	 * Room ID to pub/sub
	 */
	private final String roomId;
	/**
	 * Web socket represented from the front end
	 */
	private final ActorRef out;

	@Inject
	IMongoDB mongoDB;

	boolean userHasWriteAccess;
	User user;

	public static Props props(ActorRef out, String roomId, boolean userHasWriteAccess, User user) {
		return Props.create(ChatActor.class, () -> new ChatActor(out, roomId, userHasWriteAccess, user));
	}

	private ChatActor(ActorRef out, String roomId, boolean userHasWriteAccess, User user) {
		this.roomId = roomId;
		this.out = out;
		this.userHasWriteAccess = userHasWriteAccess;
		this.user = user;
		mediator.tell(new DistributedPubSubMediator.Subscribe(roomId, getSelf()), getSelf());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(String.class, this::onMessageReceived)
				.match(ChatActorProtocol.ChatMessage.class, this::onChatMessageReceived)
				.match(DistributedPubSubMediator.SubscribeAck.class, this::onSubscribe)
				.match(DistributedPubSubMediator.UnsubscribeAck.class, this::onUnsubscribe)
				.build();
	}

	/**
	 * Receiver of socket messages comming from the front end
	 * @param message
	 */
	public void onMessageReceived (String message) {
		if (message.equals(PING)) {
			out.tell(PONG, getSelf());
			return;
		}
		broadcast(message);
	}

	/**
	 * Chat Message Protocol message receiver
	 * @param what
	 */
	public void onChatMessageReceived (ChatActorProtocol.ChatMessage what) {
		// Don't send messages back that came from this socket
		if (getSender().equals(getSelf())) {
			return;
		}
		String message = what.getMessage();
        out.tell(message, getSelf());
	}

	/**
	 * When a subscribe message is received, this method gets called
	 * @param message
	 */
	public void onSubscribe (DistributedPubSubMediator.SubscribeAck message) {
		this.joinTheRoom();
	}

	/**
	 * When an unsubscribe message is received, this method gets called
	 * @param message
	 */
	public void onUnsubscribe (DistributedPubSubMediator.UnsubscribeAck message) {
		this.leaveTheRoom();
	}

	/**
	 * When the actor is shutting down, let the others know that I've left the room!
	 */
	@Override
	public void postStop() {
		this.leaveTheRoom();
	}

	/**
	 * Sends a simple JOINED_ROOM message
	 */
	private void joinTheRoom () {
		this.broadcast(JOINED_ROOM);
	}

	/**
	 * Sends a simple LEFT_ROOM message
	 */
	private void leaveTheRoom () {
		this.broadcast(LEFT_ROOM);
	}

	/**
	 * Publish message to the current room
	 * @param message
	 */
	private void broadcast (String message) {
		// Publish new content on this room!
		String fullMessage = user.getUsername() + ": " + message;
		if (message.equals(JOINED_ROOM) || message.equals(LEFT_ROOM)){
			fullMessage = user.getUsername() + " " + message;
			if (!userHasWriteAccess) {
				mediator.tell(new DistributedPubSubMediator.Publish(roomId, new ChatActorProtocol.ChatMessage(fullMessage)), getSelf());
			}
		}
		else if (userHasWriteAccess) {
            mediator.tell(
                    new DistributedPubSubMediator.Publish(roomId, new ChatActorProtocol.ChatMessage(fullMessage)),
                    getSelf()
            );
        }
	}
}
