package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.apigatewayv2.IHttpApi;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.route53.ARecord;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ssm.IStringParameter;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.amazon.awscdk.services.wafv2.CfnWebACL;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFront;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFrontProps;
import software.constructs.Construct;

import java.io.File;
import java.util.List;

import static com.epam.indigoeln.aws.Utils.mapOf;

public class CloudFrontStack extends NestedStack {

    @Getter
    private final Distribution distribution;

    public CloudFrontStack(@NotNull Construct scope, @NotNull String id, @Nullable Props props) {
        super(scope, id, props);

        Certificate certificate = Certificate.Builder.create(this, "certificate")
                .domainName("indigo-eln-dev.test.lifescience.opensource.epam.com")
                .validation(CertificateValidation.fromDns(props.getHostedZone()))
                .build();

        Bucket frontendCodeS3 = Bucket.Builder.create(this, "signature-frontend-s3")
                .build();

        BehaviorOptions apiBehavior = BehaviorOptions.builder()
                .origin(HttpOrigin.Builder.create(Fn.parseDomainName(props.getHttpApi().getApiEndpoint()))
                        .protocolPolicy(OriginProtocolPolicy.HTTPS_ONLY)
                        .customHeaders(mapOf("X-API-Secret", props.getApiGatewaySecret().getStringValue()))
                        .build()
                )
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .responseHeadersPolicy(ResponseHeadersPolicy.CORS_ALLOW_ALL_ORIGINS)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .build();

        Function rewriteToIndexHtmlFunction = Function.Builder.create(this, "rewrite-index-html-function")
                .runtime(FunctionRuntime.JS_2_0)
                .code(FunctionCode.fromFile(FileCodeOptions.builder()
                        .filePath("resources/cloudfront-rewrite-to-index-html-function.js")
                        .build())
                )
                .build();

        distribution = Distribution.Builder.create(this, "cloudfront")
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(S3BucketOrigin.withOriginAccessControl(frontendCodeS3))
                        .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                        .functionAssociations(List.of(
                                FunctionAssociation.builder()
                                        .eventType(FunctionEventType.VIEWER_REQUEST)
                                        .function(rewriteToIndexHtmlFunction)
                                        .build()
                        ))
                        .build()
                )
                .additionalBehaviors(mapOf(
                        "/api/*", apiBehavior,
                        "/openapi/*", apiBehavior,
                        "/swagger/*", apiBehavior
                ))
                .domainNames(List.of(props.getDomainName()))
                .certificate(certificate)
                .defaultRootObject("index.html")
                .build();
        CfnWebACL.RuleProperty ipReputationsRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesAmazonIpReputationList", 0, "AWS-AWSManagedRulesAmazonIpReputationList", List.of());
        CfnWebACL.RuleProperty commonRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesCommonRuleSet", 1, "AWS-AWSManagedRulesCommonRuleSet", List.of(
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("SizeRestrictions_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build(),
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("CrossSiteScripting_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build()
        ));
        CfnWebACL.RuleProperty knownBadInputRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesKnownBadInputsRuleSet", 2, "AWS-AWSManagedRulesKnownBadInputsRuleSet", List.of());

        CfnWebACL wafWebACL = CfnWebACL.Builder.create(this, "wafwebacl")
                .scope("CLOUDFRONT")
                .rules(List.of(ipReputationsRuleSet, commonRuleSet, knownBadInputRuleSet))
                .defaultAction(CfnWebACL.DefaultActionProperty.builder().allow(CfnWebACL.AllowActionProperty.builder().build()).build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder().cloudWatchMetricsEnabled(true).metricName("WebACLMetric").sampledRequestsEnabled(true).build())
                .build();
        new WafwebaclToCloudFront(this, "wafwebacl-cloudfront", WafwebaclToCloudFrontProps.builder()
                .existingWebaclObj(wafWebACL)
                .existingCloudFrontWebDistribution(distribution)
                .build()
        );

        File frontendCode = new File("../indigo-frontend/dist/indigo-frontend/browser");
        BucketDeployment frontendDeployment = BucketDeployment.Builder.create(this, "eln-frontend-s3-deployment")
                .sources(List.of(Source.asset(frontendCode.getPath(), AssetOptions.builder().assetHash(Utils.calculateHashCode(frontendCode)).build())))
                .destinationBucket(frontendCodeS3)
                .distribution(distribution) // invalidate distribution
                .distributionPaths(List.of("/*"))
                .role(Role.Builder.create(this, "frontend-deployment-role")
                                .assumedBy(ServicePrincipal.fromStaticServicePrincipleName("lambda.amazonaws.com"))
                                .managedPolicies(List.of(
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
                                ))
                                .build()
                )
                .build();

        ARecord.Builder.create(this, "domain-record")
                .zone(props.getHostedZone())
                .recordName("indigo-eln-dev.test.lifescience.opensource.epam.com.")
                .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
                .build();
    }

    private CfnWebACL.RuleProperty createWAFRuleSet(String vendor, String name, int priority, String metric, List<CfnWebACL.RuleActionOverrideProperty> overrides) {
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

    @Value
    public static class Props implements NestedStackProps {

        IHostedZone hostedZone;
        IHttpApi httpApi;
        String domainName;
        IStringParameter apiGatewaySecret;
    }
}
