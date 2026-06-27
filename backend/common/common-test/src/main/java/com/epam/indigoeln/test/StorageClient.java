package com.epam.indigoeln.test;

import com.google.common.io.MoreFiles;
import io.quarkus.runtime.configuration.ConfigUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public interface StorageClient {

    void mkdir(String path);
    void clearDir(String path);
    List<String> listFiles(String path);
    byte @Nullable [] read(String path);

    static StorageClient instance() {
        String bucketName = System.getProperty("eln.storage.s3.bucket");
        if (bucketName != null) {
            return new S3StorageClient(bucketName);
        } else {
            String root = ConfigUtils.getFirstOptionalValue(List.of("eln.storage.local.root"), String.class).get();
            return new FileStorageClient(Paths.get(root));
        }
    }

    @RequiredArgsConstructor
    class FileStorageClient implements StorageClient {

        private final Path root;

        @Override
        @SneakyThrows
        public void mkdir(String path) {
            Files.createDirectories(root.resolve(path));
        }

        @Override
        @SneakyThrows
        public void clearDir(String path) {
            Path dir = root.resolve(path);
            if (Files.exists(dir)) {
                MoreFiles.deleteDirectoryContents(dir);
            }
        }

        @Override
        @SneakyThrows
        public List<String> listFiles(String path) {
            Path dir = root.resolve(path);
            if (Files.exists(dir)) {
                try (Stream<Path> stream = Files.list(dir)) {
                    return stream
                            .map(x -> root.relativize(x).toString())
                            .toList();
                }
            }
            return List.of();
        }

        @Override
        @SneakyThrows
        public byte @Nullable [] read(String path) {
            Path file = root.resolve(path);
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }
            return null;
        }
    }

    @RequiredArgsConstructor
    class S3StorageClient implements StorageClient {

        private final String bucketName;
        private final S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(System.getProperty("quarkus.s3.endpoint.override")))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        @Override
        public void mkdir(String path) {
            // no op
        }

        @Override
        public void clearDir(String path) {
            List<String> files = listFiles(path);
            if (!files.isEmpty()) {
                s3.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder()
                                .objects(files.stream()
                                        .map(f -> ObjectIdentifier.builder().key(f).build())
                                        .toList()
                                )
                                .build()
                        )
                        .build()
                );
            }
        }

        @Override
        public List<String> listFiles(String path) {
            if (!path.endsWith("/")) {
                path += '/';
            }
            ListObjectsV2Response response = s3.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(path)
                    .build());
            return StreamEx.of(response.contents())
                    .map(S3Object::key)
                    .toList();
        }

        @Override
        @SneakyThrows
        public byte @Nullable [] read(String path) {
            try (ResponseInputStream<GetObjectResponse> response = s3.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build()
            )) {
                return response.readAllBytes();
            } catch (NoSuchKeyException e) {
                return null;
            }
        }
    }
}
