package com.epam.indigoeln.bingodb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Bingo databases
 */
@Configuration
@ConfigurationProperties("bingo")
public class BingoProperties {

    /**
     * Path to folder with molecule/reaction databases
     */
    private String folder;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
