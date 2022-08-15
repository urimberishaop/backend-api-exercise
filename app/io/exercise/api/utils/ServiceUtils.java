package io.exercise.api.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actions.Attributes;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

public class ServiceUtils {

	private static final String USERS_COLLECTION_NAME = "users";
	private static final String CHATROOMS_COLLECTION_NAME = "chatrooms";

	public static User getUserFrom(Http.Request request) {
		return request.attrs().get(Attributes.USER_TYPED_KEY);
	}

	/**
	 * Sets a user's readACL and writeACL based on a chatroom
	 *
	 * @param user    the user
	 * @param roomId  the roomId
	 * @param mongoDB mongo
	 * @return the user with updated access
	 */
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

	/**
	 * Unwraps a user from a RequestHeader
	 *
	 * @param request the request
	 * @param mongoDB mongo
	 * @param config  config
	 * @return the user
	 */
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

	/**
	 * returns a mongo filter that's going to be used for authorization (readACL)
	 *
	 * @param requestingUser the user whose id and roles we're going to use
	 * @return the Bson filter
	 */
	public static Bson readAccessFilter(User requestingUser) {
		return Filters.or(
			in("readACL", requestingUser.getId().toString()),
			in("readACL", requestingUser.getRoles()),
			in("writeACL", requestingUser.getId().toString()),
			in("writeACL", requestingUser.getRoles()),
			Filters.and(
				eq("readACL", new ArrayList<>()),
				eq("writeACL", new ArrayList<>())),
			Filters.and(
				eq("readACL", List.of("*")),
				eq("writeACL", List.of("*"))));
	}

	/**
	 * returns a mongo filter that's going to be used for authorization (writeACL)
	 *
	 * @param requestingUser the user whose id and roles we're going to use
	 * @return the Bson filter
	 */
	public static Bson writeAccessFilter(User requestingUser) {
		return Filters.or(
			in("writeACL", requestingUser.getId().toString()),
			in("writeACL", requestingUser.getRoles()),
			eq("writeACL", new ArrayList<>()));
	}

	/**
	 * Mongo GraphLookup in a prettier way (hiding the documents)
	 *
	 * @param from             the target collection
	 * @param startWith        expression to start
	 * @param connectFromField field to connect
	 * @param connectToField   field to connect to
	 * @param as               name of the array field
	 * @param depthField       optional Name of the depth field
	 * @return
	 */
	public static Document graphLookupEdited(String from, String startWith, String connectFromField, String connectToField, String as, String depthField) {
		return new Document("$graphLookup",
			new Document("from", from)
				.append("startWith", startWith)
				.append("connectFromField", connectFromField)
				.append("connectToField", connectToField)
				.append("as", as)
				.append("depthField", depthField));
	}

}
