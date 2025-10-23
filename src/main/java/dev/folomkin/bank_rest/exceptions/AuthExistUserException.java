package dev.folomkin.bank_rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthExistUserException extends RuntimeException {
    public AuthExistUserException(String message) {
        super(message);
    }
}
