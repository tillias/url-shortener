package com.url.shortener;

import com.url.shortener.infrastructure.DigestConfig;
import com.url.shortener.infrastructure.UrlRepository;
import com.url.shortener.infrastructure.UrlValidationException;
import io.seruco.encoding.base62.Base62;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShortenerService {

    private UrlRepository repository;
    private DigestConfig config;

    @Autowired
    public ShortenerService(UrlRepository repository, DigestConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public Url shorten(String sourceUrl) {
        final UrlValidator validator = new UrlValidator();

        if (validator.isValid(sourceUrl)) {
            return process(sourceUrl);
        } else {
            throw new UrlValidationException("Invalid source url is provided");
        }
    }

    public Url getUrlByID(String id) {
        final Optional<Url> url = repository.findById(id);
        return url.orElse(null);
    }

    private Url process(String sourceUrl) {

        final Url persistent = repository.findBySourceUrl(sourceUrl);
        if (persistent != null) {
            return persistent;
        } else {
            final Url url = generateShortcut(sourceUrl);
            return repository.save(url);
        }
    }

    private Url generateShortcut(String sourceUrl) {
        final UniformRandomProvider rnd = RandomSource.create(RandomSource.MT);
        final Base62 base62 = Base62.createInstance();


        int maxAttempts = config.getRandomMaxAttempts();
        int iteration = 0; // fail fast is better
        String digest;

        do {
            digest = generateRandomHash(rnd, base62);
            ++iteration;
        }
        while (repository.existsById(digest) && iteration < maxAttempts);

        if (iteration == maxAttempts) {
            throw new UnsupportedOperationException(String.format("Can't generate random hash using reasonable " +
                    "amount of time. Attempted %s times", iteration));
        }

        final String shortcut = config.getPrefix() + digest;
        return new Url(digest, sourceUrl, shortcut);
    }

    private String generateRandomHash(UniformRandomProvider rnd, Base62 base62) {

        final byte[] randomBytes = new byte[config.getRandomSize()];
        rnd.nextBytes(randomBytes);

        final byte[] encodedBytes = base62.encode(randomBytes);

        final String longHash = new String(encodedBytes);
        return StringUtils.substring(longHash, 0, config.getLength());
    }
}