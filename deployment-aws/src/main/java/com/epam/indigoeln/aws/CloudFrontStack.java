package com.epam.indigoeln.aws;

import com.epam.indigoeln.aws.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.cloudfront.AllowedMethods;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.CachePolicy;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.Function;
import software.amazon.awscdk.services.cloudfront.FunctionAssociation;
import software.amazon.awscdk.services.cloudfront.FunctionCode;
import software.amazon.awscdk.services.cloudfront.FunctionEventType;
import software.amazon.awscdk.services.cloudfront.FunctionRuntime;
import software.amazon.awscdk.services.cloudfront.HeadersFrameOption;
import software.amazon.awscdk.services.cloudfront.IOrigin;
import software.amazon.awscdk.services.cloudfront.OriginProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.OriginRequestPolicy;
import software.amazon.awscdk.services.cloudfront.PriceClass;
import software.amazon.awscdk.services.cloudfront.ResponseCustomHeader;
import software.amazon.awscdk.services.cloudfront.ResponseCustomHeadersBehavior;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersContentSecurityPolicy;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersContentTypeOptions;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersFrameOptions;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersPolicy;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersStrictTransportSecurity;
import software.amazon.awscdk.services.cloudfront.ResponseSecurityHeadersBehavior;
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.IUserPoolClient;
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
import java.util.Map;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class CloudFrontStack {

    @Getter
    private final Distribution distribution;

    public CloudFrontStack(Construct scope, Props props) {
        Certificate certificate = Certificate.Builder.create(scope, "certificate")
                .domainName(props.domainName)
                .validation(CertificateValidation.fromDns(props.hostedZone()))
                .build();

        Bucket frontendCodeS3 = Bucket.Builder.create(scope, "frontend-s3")
                .build();

        // One origin for the bucket, shared by every behavior that serves it: a second call would
        // synthesize a second CloudFront origin and a second OAC config for the same bucket.
        IOrigin frontendOrigin = S3BucketOrigin.withOriginAccessControl(frontendCodeS3);

        CachePolicy defaultCachePolicy = CachePolicy.Builder.create(scope, "default-cache-policy")
                .cachePolicyName("default-cache-policy-" + props.envName)
                .defaultTtl(Duration.minutes(10))
                .minTtl(Duration.seconds(0))
                .maxTtl(Duration.days(365))
                // CloudFront only compresses when Accept-Encoding is part of the cache key; the
                // behaviors' own `compress` already defaults to true, so this is the other half.
                .enableAcceptEncodingGzip(true)
                .enableAcceptEncodingBrotli(true)
                .build();

        // The compose stack on the EC2 instance, reached over plain HTTP on port 80. CloudFront will
        // not accept a self-signed origin certificate, so avoiding this hop being unencrypted would
        // mean putting an ALB in front. X-API-Secret is what proves to the services that a request
        // arrived through the CDN; nginx passes it through rather than injecting its own.
        //
        // A CloudFront origin has to be a domain name, so the instance's Elastic IP gets one.
        String originDomainName = "origin." + props.domainName;
        ARecord.Builder.create(scope, "origin-domain-record")
                .zone(props.hostedZone())
                .recordName(originDomainName + '.')
                .target(RecordTarget.fromIpAddresses(props.instanceIp()))
                .build();

        BehaviorOptions apiBehavior = BehaviorOptions.builder()
                .origin(HttpOrigin.Builder.create(originDomainName)
                        .protocolPolicy(OriginProtocolPolicy.HTTP_ONLY)
                        .httpPort(80)
                        .customHeaders(mapOf("X-API-Secret", props.apiSecret().getStringValue()))
                        .build()
                )
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .responseHeadersPolicy(ResponseHeadersPolicy.CORS_ALLOW_ALL_ORIGINS)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .build();

        // The React app, served at the root. It needs no Lambda@Edge: its CSP is hash-based, so
        // nothing has to be minted per request, the app shell is a plain S3 object, and the only
        // edge code left is the SPA rewrite below. Response headers are declarative (see the two
        // ResponseHeadersPolicies), which is what makes them survive error responses.
        File frontendCode = new File("../frontend2/dist");

        Function frontendRequestFunction = Function.Builder.create(scope, "frontend-request-function")
                .code(FunctionCode.fromInline(readFile("resources/cloudfront-viewer-request-function.js")))
                .runtime(FunctionRuntime.JS_2_0)
                .build();

        // Content-hashed filenames, so they can never go stale. No CSP: it is a document header and
        // is ignored on a subresource.
        ResponseHeadersPolicy frontendAssetsHeaders = ResponseHeadersPolicy.Builder.create(scope, "frontend-assets-headers")
                .responseHeadersPolicyName("frontend-assets-headers-" + props.envName)
                .securityHeadersBehavior(securityHeaders(null))
                .customHeadersBehavior(cacheControl("immutable, max-age=31536000"))
                .build();

        // The app shell, which every deploy replaces and every deep link resolves to. `override` on
        // cache-control is load-bearing: the bucket deployment stamps every object, index.html
        // included, with `immutable, max-age=31536000`, and without it that reaches the browser.
        ResponseHeadersPolicy frontendShellHeaders = ResponseHeadersPolicy.Builder.create(scope, "frontend-shell-headers")
                .responseHeadersPolicyName("frontend-shell-headers-" + props.envName)
                .securityHeadersBehavior(securityHeaders(cspPolicy(frontendCode)))
                .customHeadersBehavior(cacheControl("no-store"))
                .build();

        // config.json names this environment's Cognito pool, which is what lets one frontend build
        // serve every environment. Cached by the browser so it costs no round trip on most page
        // loads; the price is that a recreated pool can take that long to reach an open browser.
        ResponseHeadersPolicy frontendConfigHeaders = ResponseHeadersPolicy.Builder.create(scope, "frontend-config-headers")
                .responseHeadersPolicyName("frontend-config-headers-" + props.envName)
                .securityHeadersBehavior(securityHeaders(null))
                .customHeadersBehavior(cacheControl("max-age=3600, stale-while-revalidate=86400"))
                .build();

        BehaviorOptions frontendConfigBehavior = BehaviorOptions.builder()
                .origin(frontendOrigin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                // Not held at the edge: a cached copy is served with an Age header, which the browser
                // subtracts from max-age, so an edge copy older than that would never be cached there.
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .responseHeadersPolicy(frontendConfigHeaders)
                .build();

        BehaviorOptions frontendAssetsBehavior = BehaviorOptions.builder()
                .origin(frontendOrigin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .cachePolicy(defaultCachePolicy)
                .responseHeadersPolicy(frontendAssetsHeaders)
                .build();

        BehaviorOptions frontendBehavior = BehaviorOptions.builder()
                .origin(frontendOrigin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .cachePolicy(CachePolicy.CACHING_DISABLED) // the app shell must never be held at the edge
                .responseHeadersPolicy(frontendShellHeaders)
                .functionAssociations(List.of(
                        FunctionAssociation.builder()
                                .eventType(FunctionEventType.VIEWER_REQUEST)
                                .function(frontendRequestFunction)
                                .build()
                ))
                .build();

        distribution = Distribution.Builder.create(scope, "cloudfront")
                // Everything that is not an API call or a hashed asset: the app shell, and the SPA
                // rewrite that resolves deep links to it. Extensionless URIs match no pattern below,
                // so this is where they land.
                .defaultBehavior(frontendBehavior)
                // Order matters: CloudFront takes the first pattern that matches, in list order,
                // which is why mapOf is a LinkedHashMap. The API patterns must precede anything
                // serving the bucket, or /api/* would be answered with the app shell.
                .additionalBehaviors(mapOf(
                        entry("/api/*", apiBehavior),
                        entry("/openapi/*", apiBehavior),
                        entry("/swagger/*", apiBehavior),
                        entry("/assets/*", frontendAssetsBehavior),
                        entry("/config.json", frontendConfigBehavior)
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

        BucketDeployment.Builder.create(scope, "frontend-s3-deployment")
                // No destinationKeyPrefix: keys mirror the request URI, so the origin needs no
                // originPath. mockServiceWorker.js is never registered by the app (msw is Storybook
                // and vitest only), and csp-hashes.json is read from the local dist at synth time by
                // cspPolicy, so neither belongs at the origin — leaving index.html as the only
                // object at the bucket root.
                .sources(List.of(Source.asset(frontendCode.getPath(), AssetOptions.builder()
                        .assetHash(Utils.calculateHashCode(frontendCode))
                        .exclude(List.of("mockServiceWorker.js", "csp-hashes.json"))
                        .build()),
                        // Fetched by the app before it configures Amplify, see frontend2/src/lib/env.ts.
                        Source.jsonData("config.json", Map.of(
                                "cognitoUserPoolId", props.userPool().getUserPoolId(),
                                "cognitoClientId", props.userPoolClient().getUserPoolClientId()
                        ))))
                .destinationBucket(frontendCodeS3)
                .distribution(distribution) // invalidate distribution
                .distributionPaths(List.of("/*"))
                .cacheControl(List.of( // the shell is exempted by CACHING_DISABLED at the edge and by frontendShellHeaders at the browser
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
                .recordName(props.domainName + '.')
                .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
                .build();
    }

    /**
     * The app's own Content-Security-Policy, read at synth time. The policy is assembled by the
     * frontend build (see the csp-hashes plugin in vite.config.ts), so the one {@code vite preview}
     * enforces locally is the one CloudFront sends. CloudFront caps the value at 1783 characters.
     */
    @SneakyThrows
    private String cspPolicy(File frontendCode) {
        return new ObjectMapper()
                .readTree(frontendCode.toPath().resolve("csp-hashes.json").toFile())
                .required("policy")
                .asText();
    }

    /**
     * The security headers every frontend response carries, with the app shell's CSP folded in when
     * one is given. Assets get no CSP: it is a document header, ignored on a subresource.
     */
    private ResponseSecurityHeadersBehavior securityHeaders(@Nullable String csp) {
        ResponseSecurityHeadersBehavior.Builder builder = ResponseSecurityHeadersBehavior.builder()
                .strictTransportSecurity(ResponseHeadersStrictTransportSecurity.builder()
                        .accessControlMaxAge(Duration.seconds(63072000))
                        .includeSubdomains(true)
                        .preload(true)
                        .override(true)
                        .build())
                .contentTypeOptions(ResponseHeadersContentTypeOptions.builder()
                        .override(true)
                        .build())
                .frameOptions(ResponseHeadersFrameOptions.builder()
                        .frameOption(HeadersFrameOption.SAMEORIGIN)
                        .override(true)
                        .build());
        if (csp != null) {
            builder.contentSecurityPolicy(ResponseHeadersContentSecurityPolicy.builder()
                    .contentSecurityPolicy(csp)
                    .override(true)
                    .build());
        }
        return builder.build();
    }

    /**
     * CloudFront sends this instead of whatever the origin said. The override matters: the bucket
     * deployment stamps every object, index.html included, with {@code immutable, max-age=31536000}.
     */
    private ResponseCustomHeadersBehavior cacheControl(String value) {
        return ResponseCustomHeadersBehavior.builder()
                .customHeaders(List.of(ResponseCustomHeader.builder()
                        .header("cache-control")
                        .value(value)
                        .override(true)
                        .build()))
                .build();
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
            String envName,
            IHostedZone hostedZone,
            String instanceIp,
            String domainName,
            IStringParameter apiSecret,
            IUserPool userPool,
            IUserPoolClient userPoolClient
    ) {
    }
}
