package io.exercise.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class AuthenticationService {
    @Inject
    IMongoDB mongoDB;

    private static final String USERS_COLLECTION_NAME = "users";

    /**
     * Authenticates a user and returns a token.
     * @param user the User that's being authenticated
     * @return the JWT (token)
     */
    public CompletableFuture<String> authenticate(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Algorithm algorithm = Algorithm.HMAC256(ConfigFactory.load(this.getClass().getClassLoader()).getString("play.http.secret.key"));

                User userFound = mongoDB.getMongoDatabase()
                        .getCollection(USERS_COLLECTION_NAME, User.class)
                        .find()
                        .filter(Filters.eq("username", user.getUsername()))
                        .into(new ArrayList<>())
                        .get(0);

                if (Hash.checkPassword(user.getPassword(), userFound.getPassword())) {
                    return JWT.create()
                            .withClaim("id", userFound.getId().toString())
                            .sign(algorithm);
                }
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Wrong credentials."));
            } catch (CompletionException | IndexOutOfBoundsException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Wrong credentials."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }
}