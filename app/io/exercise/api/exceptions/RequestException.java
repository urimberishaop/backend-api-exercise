package io.exercise.api.exceptions;

import lombok.Data;
import lombok.ToString;

/**
 * Created by Agon on 12/16/2016.
 */
@ToString
public @Data class RequestException extends Exception {
	private static final long serialVersionUID = 1L;
	// either String or List<String>
    private Object description;
    private int statusCode;

    public RequestException(int statusCode, Object message) {
        super(message.toString());
        this.description = message;
        this.statusCode = statusCode;
    }
    
    @Override
    public String getMessage() {
        return description.toString();
    }

    public int getStatusCode() {
        return statusCode;
    }
}


