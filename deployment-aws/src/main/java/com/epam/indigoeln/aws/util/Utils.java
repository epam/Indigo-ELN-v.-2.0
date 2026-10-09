package com.epam.indigoeln.aws.util;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class Utils {

    public static String calculateHashCode(File location) {
        try {
            List<File> files = location.isDirectory()
                    ? Files.walk(location.toPath(), FileVisitOption.FOLLOW_LINKS).map(Path::toFile).toList()
                    : List.of(location);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (File file : files) {
                baos.write(file.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
                if (file.isFile()) {
                    try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                        baos.write(is.readAllBytes());
                    }
                }
            }
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Map.of(...) may mix the order of elements, forcing CloudFormation to do unnecessary updates; so stick to LinkedHashMap

    public static <K, V> Map<K, V> mapOf() {
        return new LinkedHashMap<>();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = mapOf();
        map.put(k1, v1);
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... entries) {
        Map<K, V> map = mapOf();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
}
