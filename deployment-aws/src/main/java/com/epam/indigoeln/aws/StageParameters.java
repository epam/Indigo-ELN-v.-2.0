package com.epam.indigoeln.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

@Data
public class StageParameters {

    private String account;
    private String region;
    @Nullable
    private String permissionBoundary;
    private String domainName;
    private String vpc;
    private List<String> securityGroups;
    private String ec2KeyPair;
    private String hostedZone;
    private String hostedZoneName;
    /** Must be the AZ the Postgres data volume lives in; a standalone EBS only attaches within one AZ. */
    private String dataVolumeAz;
    private List<String> lambdaSubnets;
    private String postgresImageTag;
    private String elnAwsImageTag;
    private String reportsAwsImageTag;
    private String signatureAwsImageTag;
    private String sampleRegistrationAwsImageTag;
    /** Shared between CloudFront's origin custom header and every service's APISecretFilter. */
    private String apiGatewaySecret;
    private String storageBucketName;

    @SneakyThrows
    public static StageParameters load(String env) {
        return new ObjectMapper().readValue(new File(env + ".json"), StageParameters.class);
    }
}
