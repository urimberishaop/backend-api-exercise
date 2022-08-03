package io.exercise.api.utils;

import io.exercise.api.actions.Attributes;
import io.exercise.api.models.User;
import play.mvc.Http;

import java.util.Optional;

public class ServiceUtils {
    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    public static String getTokenFromRequest(Http.Request request) {
        Optional<String> optionalToken = request.getHeaders().get("token");
        return optionalToken.orElse(null);
    }
}
