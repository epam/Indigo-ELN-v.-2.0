package com.epam.indigoeln.common.lambda;

import com.epam.indigoeln.common.storage.FileStorage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class S3FileStorage implements FileStorage {

    @ConfigProperty(name = "eln.storage.s3.bucket")
    String bucket;

    @Inject
    S3Client s3;

    @Override
    public void mkdir(String key) {
        // no op
    }

    @Override
    public void clear(String key) {
        log.warn("Clearing directory: {}", key);
        List<String> list = list(key);
        if (!list.isEmpty()) {
            List<ObjectIdentifier> objects = list.stream().map(k -> ObjectIdentifier.builder().key(k).build()).toList();
            DeleteObjectsResponse deleteResponse = s3.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objects).build())
                    .build()
            );
            if (deleteResponse.hasErrors()) {
                throw new RuntimeException("Failed to clear directory: " + deleteResponse.errors().stream().map(Object::toString).toList());
            }
            log.info("Cleared {} files from directory {}", deleteResponse.deleted().size(), bucket);
        }
    }

    @Override
    public List<String> list(String key) {
        if (!key.endsWith("/")) {
            key += "/";
        }
        ListObjectsV2Response response;
        List<String> result = new ArrayList<>();
        String continuationToken = null;
        do {
            ListObjectsV2Request.Builder req = ListObjectsV2Request.builder().bucket(bucket).prefix(key);
            if (continuationToken != null) req.continuationToken(continuationToken);
            response = s3.listObjectsV2(req.build());
            for (S3Object object : response.contents()) {
                result.add(object.key());
            }
            continuationToken = response.nextContinuationToken();
        } while (response.isTruncated());
        return result;
    }

    @Override
    public void put(String key, byte[] bytes) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).build(),
            RequestBody.fromBytes(bytes)
        );
    }

    @Override
    public byte[] get(String key) {
        try (ResponseInputStream<GetObjectResponse> response = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
