package com.url.shortener;

import com.url.shortener.infrastructure.DigestProperties;
import com.url.shortener.infrastructure.UrlConflictException;
import com.url.shortener.infrastructure.UrlRepository;
import com.url.shortener.infrastructure.UrlValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ShortenerServiceTests {

    @MockBean
    private UrlRepository repository;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Autowired
    private ShortenerService service;

    @Autowired
    private DigestProperties digestProperties;

    @Test(expected = UrlValidationException.class)
    public void shorten_NullUrl_ExceptionIsThrown() {
        service.shorten(null, null);
    }

    @Test(expected = UrlValidationException.class)
    public void shorten_InvalidUrl_ExceptionIsThrown() {
        service.shorten("h ttp://&example.com", null);
    }

    @Test
    public void shorten_CustomHash_Success() {
        final String sourceUrl = "http://foo.com";
        final String customHash = "test-hash";
        final Url persistent = new Url(customHash, sourceUrl);

        Mockito.when(repository.save(any(Url.class))).thenReturn(persistent);

        final Url url = service.shorten(sourceUrl, customHash);

        assertNotNull(url);
        assertEquals(customHash, url.getId());
        assertEquals(sourceUrl, url.getSourceUrl());
        assertEquals(digestProperties.getPrefix() + customHash, url.getShortcut());
    }

    @Test
    public void shorten_RandomHash_Success() {
        final String sourceUrl = "http://foo.com";

        Mockito.when(repository.save(any(Url.class))).thenAnswer((Answer<Url>) i -> i.getArgument(0));
        final Url url = service.shorten(sourceUrl, null);

        assertNotNull(url);
        assertNotNull(url.getId());
        assertNotNull(url.getShortcut());
        assertEquals(getExpectedShortcut(url.getId()), url.getShortcut());
    }

    @Test
    public void shorten_RandomHashAndAlreadyExists_ExistingUrlReturned(){
        final String sourceUrl = "http://foo.com";
        final Url expected = new Url("1", sourceUrl);

        Mockito.when(repository.findBySourceUrl(sourceUrl)).thenReturn(expected);

        final Url url = service.shorten(sourceUrl, null);
        verify(repository).findBySourceUrl(sourceUrl);
        assertSame(expected, url);
    }

    @Test
    public void shorten_CustomHashAndAlreadyExists_ExistingUrlIsReturned(){
        final String sourceUrl = "http://foo.com";
        final String hash = "1";
        final Url expected = new Url(hash, sourceUrl);

        Mockito.when(repository.findById(hash)).thenReturn(Optional.of(expected));

        final Url url = service.shorten(sourceUrl, hash);
        verify(repository).findById(hash);
        assertSame(expected, url);
    }

    @Test(expected = UrlConflictException.class)
    public void shorten_CustomHashDifferentSourceUrl_ExceptionIsThrown(){
        final String sourceUrl = "http://foo.com";
        final String hash = "1";

        Mockito.when(repository.findById(hash)).thenReturn(Optional.of(new Url(hash, "http://google.com")));

        service.shorten(sourceUrl, hash);
    }

    @Test
    public void getUrls_UrlsExist_ReturnedWithShortcut() {
        final List<Url> urls = Arrays.asList(new Url("1", "http://foo.com/1"),
                new Url("2", "http://foor.com/2"));
        Mockito.when(repository.findAll()).thenReturn(urls);

        final List<Url> actual = service.getUrls();

        assertThat(actual).isNotNull().hasSize(urls.size()).extracting(Url::getShortcut).containsOnly(
                getExpectedShortcut("1"), getExpectedShortcut("2"));
    }

    @Test(expected = UrlValidationException.class)
    public void deleteUrl_NullID_ExceptionIsThrown() {
        service.deleteUrl(null);
    }

    @Test
    public void deleteUrl_ValidID_Success() {
        final String id = "some-hash";

        service.deleteUrl(id);
        verify(repository).deleteById(stringArgumentCaptor.capture());
        assertEquals(id, stringArgumentCaptor.getValue());
    }

    @Test
    public void getUrlByID_NonExisting_NoExceptionThrown() {
        final Url url = service.getUrlByID(null);
        Assert.assertNull(url);
    }

    @Test
    public void getUrlByID_Existing_ShortcutCreated() {
        final String id = "testID";
        final Url source = new Url(id, "http://foo.com");

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(source));

        final Url url = service.getUrlByID(id);
        assertNotNull("Shortcut should be generated", url.getShortcut());

        assertEquals(getExpectedShortcut(id), url.getShortcut());
    }

    private String getExpectedShortcut(String hash) {
        return String.format("%s%s", digestProperties.getPrefix(), hash);
    }
}
