package com.url.shortener;

import com.url.shortener.infrastructure.UrlNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Api(value = "shortener", description = "Operations for shortening urls")
@CrossOrigin
@RestController
public class ShortenerController {

    private ShortenerService service;

    @Autowired
    public ShortenerController(ShortenerService service) {
        this.service = service;
    }


    @ApiOperation(value = "Performs url shortening action", response = Url.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully shortened url"),
            @ApiResponse(code = 400, message = "Invalid URL has been provided"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 409, message = "You're trying to shorten url with custom hash, but different url " +
                    "already uses this hash")
    })
    @PostMapping("/shorten-url")
    public Url shortenUrl(@RequestBody String source, @RequestParam(value = "custom-hash", required =
            false) String customHash) {
        return service.shorten(source, customHash);
    }

    @ApiOperation(value = "Gets list of all urls. ApiResponses will be described in version 2.0")
    @GetMapping("/url")
    public List<Url> getUrls() {
        return service.getUrls();
    }

    @ApiOperation(value = "Gets url using its id. ApiResponses will be described in version 2.0")
    @GetMapping("/url/{id}")
    public Url getUrl(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url == null) {
            throw new UrlNotFoundException(String.format("Url id=%s not found", id));
        }

        return url;
    }

    @ApiOperation(value = "Attempts to find url by it's hash and if found redirects. ApiResponses will be described " +
            "in version 2.0")
    @GetMapping("redirect/{id}")
    public RedirectView redirect(@PathVariable String id) {
        final Url url = service.getUrlByID(id);

        if (url != null) {
            return new RedirectView(url.getSourceUrl());
        }

        throw new UrlNotFoundException(String.format("Shortened url with id=%s is not found", id));
    }

    @ApiOperation(value = "Deletes url. ApiResponses will be described in version 2.0")
    @DeleteMapping("url/{id}")
    public void deleteUrl(@PathVariable String id) {
        service.deleteUrl(id);
    }
}
