package com.epam.indigoeln.test;

import com.epam.indigoeln.common.config.ErrorDTO;
import com.epam.indigoeln.common.config.UserHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.base.MoreObjects;
import feign.*;
import feign.codec.StringDecoder;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs3.JAXRS3Contract;
import feign.slf4j.Slf4jLogger;
import io.vertx.core.json.jackson.VertxModule;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FeignUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new VertxModule())
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static final ObjectMapper OBJECT_MAPPER_FORMATTED = OBJECT_MAPPER.copy()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Getter
    @Setter
    @Nullable
    private static String apiSecret;

    @Getter
    @Setter
    private static Response lastResponse;

    public static <T> T buildFeignClient(URI baseURL, Class<T> klass, AtomicReference<String> testUsername, AtomicReference<String> authorization) {
        return Feign.builder()
                .client(new ApacheHttpClient())
                .options(new Request.Options(Duration.ofSeconds(1), Duration.ofDays(1), false))
                .contract(new JAXRS3Contract())
                .encoder(new RequestEncoder(new FormEncoder(new JacksonEncoder(OBJECT_MAPPER))))
                .decoder(new ResponseDecoder(new StringDecoder(), new JacksonDecoder(OBJECT_MAPPER)))
                .requestInterceptor(request -> {
                    extractParam(request, "pageNo", "pageNo=", ",");
                    extractParam(request, "pageSize", "pageSize=", ")");
                    // use admin by default; to allow testing without need to specify username, and also to enable calls from setUp/tearDown methods, where @TestSecurity doesn't work
                    request.header(UserHolder.X_TEST_AUTHORIZATION, MoreObjects.firstNonNull(testUsername.get(), BaseTest.ADMIN_USERNAME));
                    request.header(HttpHeaders.AUTHORIZATION, authorization.get());
                    if (apiSecret != null) {
                        request.header("X-API-Secret", apiSecret);
                    }
                })
                .logLevel(Logger.Level.FULL)
//                .logger(new BinaryAwareSlf4jLogger("feign"))
                .logger(new Slf4jLogger("feign"))
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder((methodKey, response) -> {
                    String body = "";
                    try (InputStream is = response.body().asInputStream()) {
                        body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception ignore) {
                    }
                    try {
                        List<ErrorDTO> errors = OBJECT_MAPPER.readValue(body, new TypeReference<>() {});
                        return new APICallException(response.status(), response.reason(), errors);
                    } catch (Exception e) {
                        throw new RuntimeException("Server didn't return a valid JSON error response: " + body, e);
                    }
                })
                .target(klass, baseURL.toString());
//        client = RestClientBuilder.newBuilder().baseUri(baseURL).build(ELNClient.class);
    }

    // workaround for Feign client incorrect handling of @BeanParam
    private static void extractParam(RequestTemplate request, String paramName, String substringBefore, String substringAfter) {
        Collection<String> values = request.queries().get(paramName);
        if (values != null) {
            String decoded = URLDecoder.decode(values.iterator().next(), StandardCharsets.UTF_8);
            int p0 = decoded.indexOf(substringBefore);
            int p1 = decoded.indexOf(substringAfter);
            if (p0 == -1 || p1 == -1) {
                throw new IllegalStateException("Incorrectly formatted param: " + decoded);
            }
            String value = decoded.substring(p0 + substringBefore.length(), p1);
            request.query(paramName, (String[]) null);
            if (!value.equals("null")) {
                request.query(paramName, value);
            }
        }
    }
}
