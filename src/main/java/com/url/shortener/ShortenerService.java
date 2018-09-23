package com.url.shortener;

import com.url.shortener.infrastructure.DigestProperties;
import com.url.shortener.infrastructure.UrlConflictException;
import com.url.shortener.infrastructure.UrlRepository;
import com.url.shortener.infrastructure.UrlValidationException;
import io.seruco.encoding.base62.Base62;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShortenerService {

    private UrlRepository repository;
    private DigestProperties digestProperties;

    @Autowired
    public ShortenerService(UrlRepository repository, DigestProperties digestProperties) {
        this.repository = repository;
        this.digestProperties = digestProperties;
    }

    Url shorten(String sourceUrl, String customHash) {
        final UrlValidator validator = new UrlValidator();

        if (validator.isValid(sourceUrl)) {
            return process(sourceUrl, customHash);
        } else {
            throw new UrlValidationException("Invalid source url is provided");
        }
    }

    List<Url> getUrls() {
        final List<Url> urls = repository.findAll();
        urls.forEach(this::enrichWithShortcut);
        return urls;
    }

    Url getUrlByID(String id) {
        final Optional<Url> url = repository.findById(id);
        return enrichWithShortcut(url.orElse(null));
    }

    boolean deleteUrl(String id) {
        if (id == null) {
            throw new UrlValidationException("Url id can't be null");
        }

        repository.deleteById(id);
        return true;
    }

    private Url process(String sourceUrl, String customHash) {
        Url result;

        if (StringUtils.isBlank(customHash)) {
            result = generateWithRandomHash(sourceUrl);
        } else {
            result = generateUsingProvidedHash(sourceUrl, customHash);
        }

        return enrichWithShortcut(result);
    }

    /**
     * Attempts to find already generated shortcut first to preserve storage
     *
     * @param sourceUrl to be shortened
     * @return shortcut for sourceUrl
     */
    private Url generateWithRandomHash(String sourceUrl) {
        final Url persistent = repository.findBySourceUrl(sourceUrl);
        if (persistent != null) {
            return persistent;
        } else {
            final Url url = generateShortcut(sourceUrl);
            return repository.save(url);
        }
    }

    /**
     * Attempts to generate shortened url using client-provided hash.
     *
     * @param sourceUrl  to be shortened
     * @param customHash provided by client
     * @return shortcut for sourceUrl
     */
    private Url generateUsingProvidedHash(String sourceUrl, String customHash) {
        final Optional<Url> persistent = repository.findById(customHash);

        if (persistent.isPresent()) {
            final Url url = persistent.get();

            if (StringUtils.equals(url.getSourceUrl(), sourceUrl)) {
                return persistent.get();
            } else {
                throw new UrlConflictException(String.format("There already exists another url with provided short " +
                                "code=[%s]. Existing url won't be exposed with this response to avoid possible " +
                                "security breaches when attacker can get access to other urls brut-forcing hashes",
                        customHash));
            }
        } else {
            return repository.save(new Url(customHash, sourceUrl));
        }
    }

    private Url generateShortcut(String sourceUrl) {
        final UniformRandomProvider rnd = RandomSource.create(RandomSource.MT);
        final Base62 base62 = Base62.createInstance();


        int maxAttempts = digestProperties.getRandomMaxAttempts();
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

        return new Url(digest, sourceUrl);
    }

    private String generateRandomHash(UniformRandomProvider rnd, Base62 base62) {

        final byte[] randomBytes = new byte[digestProperties.getRandomSize()];
        rnd.nextBytes(randomBytes);

        final byte[] encodedBytes = base62.encode(randomBytes);

        final String longHash = new String(encodedBytes);
        return StringUtils.substring(longHash, 0, digestProperties.getRandomLength());
    }

    private Url enrichWithShortcut(Url source) {
        if (source == null) {
            return null;
        }

        source.setShortcut(digestProperties.getPrefix() + source.getId());
        return source;
    }
}
