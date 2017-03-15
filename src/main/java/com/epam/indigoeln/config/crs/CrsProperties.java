package com.epam.indigoeln.config.crs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("crs")
public class CrsProperties {

    private String searchServiceUrl;
    private String registrationServiceUrl;
    private String username;
    private String password;

    public String getSearchServiceUrl() {
        return searchServiceUrl;
    }

    public void setSearchServiceUrl(String searchServiceUrl) {
        this.searchServiceUrl = searchServiceUrl;
    }

    public String getRegistrationServiceUrl() {
        return registrationServiceUrl;
    }

    public void setRegistrationServiceUrl(String registrationServiceUrl) {
        this.registrationServiceUrl = registrationServiceUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
