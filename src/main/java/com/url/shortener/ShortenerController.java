package com.url.shortener;

import com.url.shortener.infrastructure.UrlNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShortenerController {

    private ShortenerService service;

    @Autowired
    public ShortenerController(ShortenerService service) {
        this.service = service;
    }

    @PostMapping("/shorten-url")
    public Url shortenUrl(@RequestBody String source){
        return service.shorten(source);
    }

    @GetMapping("/url/{id}")
    public Url getUrl(@PathVariable String id){
        final Url url = service.getUrlByID(id);

        if (url == null){
            throw new UrlNotFoundException();
        }

        return url;
    }


}
