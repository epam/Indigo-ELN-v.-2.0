package com.epam.indigoeln.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("indigoeln.security")
public class IntSecurityProperties {

    private String remembermeKey;

    public String getRemembermeKey() {
        return remembermeKey;
    }

    public void setRemembermeKey(String remembermeKey) {
        this.remembermeKey = remembermeKey;
    }
}
