package com.url.shortener;

import com.url.shortener.infrastructure.UrlNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
public class ShortenerController {

    // Needed for Heroku / Openshift deployments
    private static final HttpHeaders CORS_HEADERS = create();

    private ShortenerService service;

    @Autowired
    public ShortenerController(ShortenerService service) {
        this.service = service;
    }

    private static HttpHeaders create() {
        final HttpHeaders headers = new HttpHeaders();

        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,PUT,POST");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Depth, User-Agent, X-File-Size, X-Requested-With, " +
                "X-Requested-By, If-Modified-Since, X-File-Name, Cache-Control");

        return headers;
    }

    @PostMapping("/shorten-url")
    public ResponseEntity<Url> shortenUrl(@RequestBody String source) {
        final Url url = service.shorten(source);
        return new ResponseEntity<>(url, CORS_HEADERS, HttpStatus.OK);
    }

    @GetMapping("/url")
    public ResponseEntity<List<Url>> getUrls() {
        final List<Url> urls = service.getUrls();
        return new ResponseEntity<>(urls, CORS_HEADERS, HttpStatus.OK);
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<Url> getUrl(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url == null) {
            return ResponseEntity.notFound().build();
        }

        return new ResponseEntity<>(url, CORS_HEADERS, HttpStatus.OK);
    }

    @GetMapping("redirect/{id}")
    public RedirectView redirect(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url != null) {
            return new RedirectView(url.getSourceUrl());
        }

        throw new UrlNotFoundException(String.format("Shortened url with id=%s is not found", id));
    }

    @GetMapping("/get-env/{envName}")
    public String getEnv(@PathVariable String envName) {
        return System.getenv(envName);
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handle() {
        return new ResponseEntity(CORS_HEADERS, HttpStatus.OK);
    }

}
