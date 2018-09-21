package com.url.shortener.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UrlConflictException extends RuntimeException {
    public UrlConflictException(String message) {
        super(message);
    }
}
