package com.pear0.td;

/**
 * Created by William on 3/25/2017.
 */
public class UserException extends RuntimeException {

    public UserException(String friendlyMessage) {
        super(friendlyMessage);
    }

    public UserException(String friendlyMessage, Throwable cause) {
        super(friendlyMessage, cause);
    }

    public String getFriendlyMessage() {
        return getMessage();
    }

}
