package com.url.shortener;

import com.url.shortener.infrastructure.UrlNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@CrossOrigin
@RestController
public class ShortenerController {

    private ShortenerService service;

    @Autowired
    public ShortenerController(ShortenerService service) {
        this.service = service;
    }

    @PostMapping("/shorten-url")
    public Url shortenUrl(@RequestBody String source, @RequestParam(value = "custom-hash", required =
            false) String customHash) {
        return service.shorten(source, customHash);
    }

    @GetMapping("/url")
    public List<Url> getUrls() {
        return service.getUrls();
    }

    @GetMapping("/url/{id}")
    public Url getUrl(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url == null) {
            throw new UrlNotFoundException(String.format("Url id=%s not found", id));
        }

        return url;
    }

    @GetMapping("redirect/{id}")
    public RedirectView redirect(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url != null) {
            return new RedirectView(url.getSourceUrl());
        }

        throw new UrlNotFoundException(String.format("Shortened url with id=%s is not found", id));
    }
}
