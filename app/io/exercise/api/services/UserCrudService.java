package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import org.bson.types.ObjectId;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class UserCrudService {
    @Inject
    IMongoDB mongoDB;

    private static final String USERS_COLLECTION_NAME = "users";

    public CompletableFuture<List<User>> all() {
        return CompletableFuture.supplyAsync(() -> {
            try {
            return mongoDB.getMongoDatabase()
                    .getCollection(USERS_COLLECTION_NAME, User.class)
                    .find()
                    .into(new ArrayList<>());
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<User> create(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                user.setPassword(Hash.createPassword(user.getPassword()));
                mongoDB.getMongoDatabase()
                        .getCollection(USERS_COLLECTION_NAME, User.class)
                        .insertOne(user);
                return user;
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<User> update(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                user.setId(null);
                return mongoDB.getMongoDatabase()
                        .getCollection(USERS_COLLECTION_NAME, User.class)
                        .findOneAndReplace(Filters.eq("_id", new ObjectId(id)), user);
            } catch (CompletionException e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found."));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, e));
            }
        });
    }

    public CompletableFuture<User> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection(USERS_COLLECTION_NAME, User.class)
                        .findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "User not found."));
            }
        });
    }
}
