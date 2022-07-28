package io.exercise.api.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.typesafe.config.ConfigFactory;
import io.exercise.api.models.AuthenticationUser;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import play.libs.Json;
import play.mvc.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.exercise.api.utils.Hash;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject
    IMongoDB mongoDB;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    public Result authenticate(Http.Request request) {
        ObjectNode response = Json.newObject();
        try {

            JsonNode body = request.body().asJson();
            String token = "";
            Algorithm algorithm = Algorithm.HMAC256(ConfigFactory.load().getString("play.http.secret.key"));


            if (request.header("token").isEmpty()) {

                FindIterable<AuthenticationUser> find = mongoDB.getMongoDatabase()
                        .getCollection("authentication", AuthenticationUser.class)
                        .find()
                        .filter(Filters.eq("username", body.get("username").asText()));

                List<AuthenticationUser> userFound = find.into(new ArrayList<>());

                if (userFound.get(0) == null) {
                    return badRequest("Wrong credentials.");
                } else if (Hash.checkPassword(body.get("password").asText(), userFound.get(0).getPassword())) {
                    token = JWT.create()
                            .withClaim("id", userFound.get(0).getId())
                            .sign(algorithm);
                    response.put("token", token);
                }
            } else {
                token = request.header("token").get();
                token = token.replace("bearer ", "");
                JWTVerifier verifier = JWT.require(algorithm)
                        .build();
                DecodedJWT jwt = verifier.verify(token);
                return ok(Json.toJson("Authentication successful."));
            }
        } catch (SignatureVerificationException | JWTDecodeException e) {
            return ok(Json.toJson("Wrong token."));
        } catch (JWTCreationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ok(response);
    }
}
