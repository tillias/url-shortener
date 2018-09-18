package com.url.shortener.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Url not found")
public class UrlNotFoundException extends RuntimeException  {
}
