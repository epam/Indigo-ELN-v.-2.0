package com.epam.indigoeln.aws;

import com.epam.indigoeln.aws.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.apigatewayv2.IHttpApi;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.experimental.EdgeFunction;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.route53.ARecord;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.CacheControl;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ssm.IStringParameter;
import software.amazon.awscdk.services.wafv2.CfnWebACL;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFront;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFrontProps;
import software.constructs.Construct;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class CloudFrontStack {

    @Getter
    private final Distribution distribution;

    public CloudFrontStack(Construct scope, Props props) {
        Certificate certificate = Certificate.Builder.create(scope, "certificate")
                .domainName("indigo-eln-dev.test.lifescience.opensource.epam.com")
                .validation(CertificateValidation.fromDns(props.hostedZone()))
                .build();

        Bucket frontendCodeS3 = Bucket.Builder.create(scope, "signature-frontend-s3")
                .build();

        CachePolicy defaultCachePolicy = CachePolicy.Builder.create(scope, "default-cache-policy")
                .cachePolicyName("default-cache-policy")
                .defaultTtl(Duration.minutes(10))
                .minTtl(Duration.seconds(0))
                .maxTtl(Duration.days(365))
                .build();

        BehaviorOptions apiBehavior = BehaviorOptions.builder()
                .origin(HttpOrigin.Builder.create(Fn.parseDomainName(props.httpApi().getApiEndpoint()))
                        .protocolPolicy(OriginProtocolPolicy.HTTPS_ONLY)
                        .customHeaders(mapOf("X-API-Secret", props.apiGatewaySecret().getStringValue()))
                        .build()
                )
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .responseHeadersPolicy(ResponseHeadersPolicy.CORS_ALLOW_ALL_ORIGINS)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .build();

        File frontendCode = new File("../indigo-frontend/dist/indigo-frontend/browser");
//        File frontendCode = new File("/home/user/Work/indigoeln-frontend/indigo-frontend/dist/indigo-frontend/browser");

        EdgeFunction indexViewerRequestFunction = EdgeFunction.Builder.create(scope, "index-viewer-request-function")
                .runtime(Runtime.NODEJS_22_X)
                .handler("index.handler")
                .code(htmlGeneratorLambdaCode(frontendCode))
                .build();

        Function staticAssetsResponseFunction = Function.Builder.create(scope, "static-assets-response-function")
                .code(FunctionCode.fromInline(readFile("resources/cloudfront-static-assets-response-function.js")))
                .runtime(FunctionRuntime.JS_2_0)
                .build();

        BehaviorOptions staticAssetsBehavior = BehaviorOptions.builder()
                .origin(S3BucketOrigin.withOriginAccessControl(frontendCodeS3))
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .cachePolicy(defaultCachePolicy)
                .functionAssociations(List.of(
                        FunctionAssociation.builder()
                                .eventType(FunctionEventType.VIEWER_RESPONSE)
                                .function(staticAssetsResponseFunction)
                                .build()
                ))
                .build();

        distribution = Distribution.Builder.create(scope, "cloudfront")
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(S3BucketOrigin.withOriginAccessControl(frontendCodeS3))
                        .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                        .cachePolicy(CachePolicy.CACHING_DISABLED)
                        .edgeLambdas(List.of(
                                EdgeLambda.builder()
                                        .eventType(LambdaEdgeEventType.VIEWER_REQUEST)
                                        .functionVersion(indexViewerRequestFunction.getCurrentVersion())
                                        .build()
                        ))
                        .build()
                )
                .additionalBehaviors(mapOf(
                        entry("*.*", staticAssetsBehavior),
                        entry("/api/*", apiBehavior),
                        entry("/openapi/*", apiBehavior),
                        entry("/swagger/*", apiBehavior)
                ))
                .domainNames(List.of(props.domainName()))
                .certificate(certificate)
                .priceClass(PriceClass.PRICE_CLASS_200)
                .build();

        CfnWebACL.RuleProperty ipReputationsRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesAmazonIpReputationList", 0, List.of());
        CfnWebACL.RuleProperty commonRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesCommonRuleSet", 1, List.of(
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("SizeRestrictions_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build(),
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("CrossSiteScripting_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build(),
                // Count instead of block so we can re-block selectively via label matching below
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("EC2MetaDataSSRF_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build()
        ));
        CfnWebACL.RuleProperty knownBadInputRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesKnownBadInputsRuleSet", 2, List.of());

        // Re-block EC2MetaDataSSRF_BODY for all paths except /api/eln/incidents, which legitimately receives URLs in the body
        CfnWebACL.RuleProperty ssrfReblockRule = createLabelReblockExceptPath(
                "reblock-ssrf-except-incidents", 10,
                "awswaf:managed:aws:core-rule-set:EC2MetaDataSSRF_BODY",
                "/api/eln/incidents"
        );

        CfnWebACL wafWebACL = CfnWebACL.Builder.create(scope, "wafwebacl")
                .scope("CLOUDFRONT")
                .rules(List.of(ipReputationsRuleSet, commonRuleSet, knownBadInputRuleSet, ssrfReblockRule))
                .defaultAction(CfnWebACL.DefaultActionProperty.builder().allow(CfnWebACL.AllowActionProperty.builder().build()).build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder().cloudWatchMetricsEnabled(true).metricName("WebACLMetric").sampledRequestsEnabled(true).build())
                .build();
        new WafwebaclToCloudFront(scope, "wafwebacl-cloudfront", WafwebaclToCloudFrontProps.builder()
                .existingWebaclObj(wafWebACL)
                .existingCloudFrontWebDistribution(distribution)
                .build()
        );

        BucketDeployment frontendDeployment = BucketDeployment.Builder.create(scope, "eln-frontend-s3-deployment")
                .sources(List.of(Source.asset(frontendCode.getPath(), AssetOptions.builder().assetHash(Utils.calculateHashCode(frontendCode)).build())))
                .destinationBucket(frontendCodeS3)
                .distribution(distribution) // invalidate distribution
                .distributionPaths(List.of("/*"))
                .cacheControl(List.of( // controls CloudFront edge caching; resources will be invalidated on redeploys
                        CacheControl.immutable(),
                        CacheControl.maxAge(Duration.days(365))
                ))
                .role(Role.Builder.create(scope, "frontend-deployment-role")
                        .assumedBy(ServicePrincipal.fromStaticServicePrincipleName("lambda.amazonaws.com"))
                        .managedPolicies(List.of(
                                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
                        ))
                        .build()
                )
                .memoryLimit(1024)
                .ephemeralStorageSize(Size.mebibytes(2048))
                .build();

        ARecord.Builder.create(scope, "domain-record")
                .zone(props.hostedZone())
                .recordName("indigo-eln-dev.test.lifescience.opensource.epam.com.")
                .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
                .build();
    }

    @SneakyThrows
    private Code htmlGeneratorLambdaCode(File frontendCode) {
        String indexHtml = Files.readString(
                frontendCode.toPath().resolve("index.html"),
                StandardCharsets.UTF_8
        );
        String jsonHtml = new ObjectMapper().writeValueAsString(indexHtml);
        String template = Files.readString(
                Paths.get("resources/cloudfront-index-viewer-request-function.js"),
                StandardCharsets.UTF_8
        );
        return Code.fromInline(template.replace("{{INDEX_HTML}}", jsonHtml));
    }

    @SneakyThrows
    private String readFile(String path) {
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }

    private CfnWebACL.RuleProperty createWAFRuleSet(String vendor, String name, int priority, List<CfnWebACL.RuleActionOverrideProperty> overrides) {
        return CfnWebACL.RuleProperty.builder()
                .name("rule-" + name)
                .priority(priority)
                .statement(CfnWebACL.StatementProperty.builder()
                        .managedRuleGroupStatement(CfnWebACL.ManagedRuleGroupStatementProperty.builder()
                                .name(name)
                                .vendorName(vendor)
                                .ruleActionOverrides(overrides)
                                .build())
                        .build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder()
                        .sampledRequestsEnabled(true)
                        .cloudWatchMetricsEnabled(true)
                        .metricName("metric-" + name)
                        .build())
                .overrideAction(CfnWebACL.OverrideActionProperty.builder()
                        .none(mapOf())
                        .build())
                .build();
    }

    private CfnWebACL.RuleProperty createLabelReblockExceptPath(String name, int priority, String label, String excludedPath) {
        return CfnWebACL.RuleProperty.builder()
                .name(name)
                .priority(priority)
                .statement(CfnWebACL.StatementProperty.builder()
                        .andStatement(CfnWebACL.AndStatementProperty.builder()
                                .statements(List.of(
                                        CfnWebACL.StatementProperty.builder()
                                                .labelMatchStatement(CfnWebACL.LabelMatchStatementProperty.builder().scope("LABEL").key(label).build())
                                                .build(),
                                        CfnWebACL.StatementProperty.builder()
                                                .notStatement(CfnWebACL.NotStatementProperty.builder()
                                                        .statement(CfnWebACL.StatementProperty.builder()
                                                                .byteMatchStatement(CfnWebACL.ByteMatchStatementProperty.builder()
                                                                        .searchString(excludedPath)
                                                                        .fieldToMatch(CfnWebACL.FieldToMatchProperty.builder().uriPath(mapOf()).build())
                                                                        .textTransformations(List.of(
                                                                                CfnWebACL.TextTransformationProperty.builder().priority(0).type("NONE").build()
                                                                        ))
                                                                        .positionalConstraint("STARTS_WITH")
                                                                        .build())
                                                                .build())
                                                        .build())
                                                .build()
                                ))
                                .build())
                        .build())
                .action(CfnWebACL.RuleActionProperty.builder()
                        .block(CfnWebACL.BlockActionProperty.builder().build())
                        .build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder()
                        .sampledRequestsEnabled(true)
                        .cloudWatchMetricsEnabled(true)
                        .metricName("metric-" + name)
                        .build())
                .build();
    }

    public record Props(
            IHostedZone hostedZone,
            IHttpApi httpApi,
            String domainName,
            IStringParameter apiGatewaySecret
    ) {
    }
}
