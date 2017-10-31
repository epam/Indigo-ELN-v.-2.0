package com.epam.indigoeln.bingodb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bingo")
public class BingoProperties {

    @Getter
    @Setter
    private String folder;
}
