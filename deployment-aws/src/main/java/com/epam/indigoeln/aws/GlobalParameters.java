package com.epam.indigoeln.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

@Data
public class GlobalParameters {

    private String account;
    private String region;
    @Nullable
    private String permissionBoundary;
    private String vpc;
    private List<String> securityGroups;
    private String ec2KeyPair;
    private String hostedZone;
    private String hostedZoneName;
    private String sonarDomainName;
    private String sonarPostgresMasterUsername;
    private String sonarImage;

    @SneakyThrows
    public static GlobalParameters load() {
        return new ObjectMapper().readValue(new File("global.json"), GlobalParameters.class);
    }
}
