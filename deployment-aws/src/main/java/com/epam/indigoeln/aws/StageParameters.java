package com.epam.indigoeln.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

@Data
public class StageParameters {

    private String account;
    private String region;
    private String permissionBoundary;
    private String postgresMasterUsername;
    private String domainName;
    private String vpc;
    private List<String> securityGroups;
    private String ec2KeyPair;
    private String hostedZone;
    private String hostedZoneName;

    @SneakyThrows
    public static StageParameters load(String env) {
        return new ObjectMapper().readValue(new File(env + ".json"), StageParameters.class);
    }
}
