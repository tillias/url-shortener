package com.url.shortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ShortenerApplicationTests {

    @Autowired
    private ShortenerController controller;

    @Autowired
    private ShortenerService service;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(service).isNotNull();
    }

}
