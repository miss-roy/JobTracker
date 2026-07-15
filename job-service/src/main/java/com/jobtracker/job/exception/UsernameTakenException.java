package com.jobtracker.job.exception;

/**
 * Thrown when registering a username that already exists. Mapped to HTTP 409.
 */
public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String username) {
        super("Username already taken: " + username);
    }
}
