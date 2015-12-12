package com.epam.indigo.bingodb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bingodb")
public class BingoProperties {

    private String databaseFolder;
    private String logsFolder;

    public String getDatabaseFolder() {
        return databaseFolder;
    }

    public void setDatabaseFolder(String databaseFolder) {
        this.databaseFolder = databaseFolder;
    }

    public String getLogsFolder() {
        return logsFolder;
    }

    public void setLogsFolder(String logsFolder) {
        this.logsFolder = logsFolder;
    }
}
