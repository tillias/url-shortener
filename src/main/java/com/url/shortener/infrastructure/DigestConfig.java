package com.url.shortener.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:digest.properties")
@ConfigurationProperties(prefix = "digest")
public class DigestConfig {
    private int randomSize;
    private int randomMaxAttempts;
    private int length;
    private String prefix;

    public int getRandomSize() {
        return randomSize;
    }

    public void setRandomSize(int randomSize) {
        this.randomSize = randomSize;
    }

    public int getRandomMaxAttempts() {
        return randomMaxAttempts;
    }

    public void setRandomMaxAttempts(int randomMaxAttempts) {
        this.randomMaxAttempts = randomMaxAttempts;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
