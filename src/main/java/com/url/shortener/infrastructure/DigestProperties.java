package com.url.shortener.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("digest")
public class DigestProperties {
    private int randomMaxAttempts;
    private int randomSize;
    private int randomLength;
    private String prefix;

    public int getRandomMaxAttempts() {
        return randomMaxAttempts;
    }

    public void setRandomMaxAttempts(int randomMaxAttempts) {
        this.randomMaxAttempts = randomMaxAttempts;
    }

    public int getRandomSize() {
        return randomSize;
    }

    public void setRandomSize(int randomSize) {
        this.randomSize = randomSize;
    }

    public int getRandomLength() {
        return randomLength;
    }

    public void setRandomLength(int randomLength) {
        this.randomLength = randomLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
