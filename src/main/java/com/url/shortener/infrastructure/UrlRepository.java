package com.url.shortener.infrastructure;

import com.url.shortener.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlRepository extends MongoRepository<Url, String> {
    Url findBySourceUrl(String sourceUrl);
}
