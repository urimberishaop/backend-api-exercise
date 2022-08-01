package io.exercise.api.utils;

import io.exercise.api.actions.Attributes;
import io.exercise.api.models.User;
import play.mvc.Http;

public class ServiceUtils {
    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }
}
