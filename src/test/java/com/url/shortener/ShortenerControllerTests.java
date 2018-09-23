package com.url.shortener;

import com.url.shortener.infrastructure.UrlValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ShortenerControllerTests {

    @MockBean
    private ShortenerService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shortenUrl_InvalidUrl_BadRequest() throws Exception {
        final String sourceUrl = "!foo";
        Mockito.when(service.shorten(sourceUrl, null)).thenThrow(UrlValidationException.class);
        mockMvc.perform(post("/shorten-url").content(sourceUrl)).andDo(print()).andExpect(status().isBadRequest());
        verify(service).shorten(sourceUrl, null);
    }

    @Test
    public void shortenUrl_RandomHash_Success() throws Exception {
        final String sourceUrl = "http://google.com";
        final Url expected = new Url("1", sourceUrl);
        Mockito.when(service.shorten(sourceUrl, null)).thenReturn(expected);

        mockMvc.perform(post("/shorten-url").content(sourceUrl)).andDo(print()).andExpect(status().isOk()).
                andExpect(content().string("{\"id\":\"1\",\"sourceUrl\":\"http://google.com\",\"shortcut\":null}"));
        verify(service).shorten(sourceUrl, null);
    }

    @Test
    public void shortenUrl_CustomHash_Success() throws Exception {
        final String sourceUrl = "http://google.com";
        final String hash = "1";
        Mockito.when(service.shorten(sourceUrl, hash)).thenReturn(new Url(hash, sourceUrl));

        mockMvc.perform(post("/shorten-url").content(sourceUrl).param("custom-hash", hash)).andDo(print()).andExpect(status().isOk()).
                andExpect(content().string("{\"id\":\"1\",\"sourceUrl\":\"http://google.com\",\"shortcut\":null}"));
        verify(service).shorten(sourceUrl, hash);
    }

    @Test
    public void getUrls_Success() throws Exception {
        final List<Url> urls = Arrays.asList(new Url("1", "http://foo.com/1"),
                new Url("2", "http://foor.com/2"));
        Mockito.when(service.getUrls()).thenReturn(urls);

        mockMvc.perform(get("/url")).andDo(print()).andExpect(status().isOk()).
                andExpect(content().string("[{\"id\":\"1\",\"sourceUrl\":\"http://foo.com/1\",\"shortcut\":null}," +
                        "{\"id\":\"2\",\"sourceUrl\":\"http://foor.com/2\",\"shortcut\":null}]"));
        verify(service).getUrls();
    }

    @Test
    public void getUrl_NotExists_NotFound() throws Exception {
        Mockito.when(service.getUrlByID(any())).thenReturn(null);

        mockMvc.perform(get("/url/1")).andDo(print()).andExpect(status().isNotFound());
        verify(service).getUrlByID("1");
    }

    @Test
    public void getUrl_Existing_Success() throws Exception {
        final Url url = new Url("1", "http://google.com");
        Mockito.when(service.getUrlByID(url.getId())).thenReturn(url);

        mockMvc.perform(get("/url/1")).andDo(print()).andExpect(status().isOk()).andExpect(content().string("{\"id" +
                "\":\"1\",\"sourceUrl\":\"http://google.com\",\"shortcut\":null}"));
        verify(service).getUrlByID(url.getId());
    }

    @Test
    public void redirect_NonExisting_NotFound() throws Exception {
        Mockito.when(service.getUrlByID(any())).thenReturn(null);

        mockMvc.perform(get("/redirect/1")).andDo(print()).andExpect(status().isNotFound());
        verify(service).getUrlByID("1");
    }

    @Test
    public void redirect_Existing_Redirected() throws Exception {
        final Url url = new Url("1", "http://google.com");
        Mockito.when(service.getUrlByID(url.getId())).thenReturn(url);

        mockMvc.perform(get("/redirect/1")).andDo(print()).andExpect(status().isFound()).andExpect(header().string(
                "Location", url.getSourceUrl())).andExpect(redirectedUrl(url.getSourceUrl()));
        verify(service).getUrlByID("1");
    }

    @Test
    public void deleteUrl_Success() throws Exception {
        Mockito.when(service.deleteUrl(any())).thenReturn(true);

        mockMvc.perform(delete("/url/1")).andDo(print()).andExpect(status().isOk());
        verify(service).deleteUrl("1");
    }
}
