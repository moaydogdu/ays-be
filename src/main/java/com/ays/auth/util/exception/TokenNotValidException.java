package com.ays.auth.util.exception;

import com.ays.common.util.exception.AysAuthException;

import java.io.Serial;

/**
 * Exception thrown when a token is not valid.
 */
public class TokenNotValidException extends AysAuthException {

    /**
     * Unique identifier for serialization.
     */
    @Serial
    private static final long serialVersionUID = -5404410121820902017L;

    /**
     * Constructs a new TokenNotValidException with the specified JWT and cause.
     *
     * @param jwt   The JWT (JSON Web Token) that is not valid.
     * @param cause The cause of the exception.
     */
    public TokenNotValidException(String jwt, Throwable cause) {
        super("TOKEN IS NOT VALID! token: " + jwt, cause);
    }

}
