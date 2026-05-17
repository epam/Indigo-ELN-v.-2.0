package com.epam.indigoeln.common.util;

import lombok.SneakyThrows;
import org.jboss.resteasy.reactive.common.headers.HeaderUtil;
import org.jspecify.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ContentDispositionUtil {

    @Nullable
    public static String extractFilename(@Nullable Collection<?> contentDisposition) {
        return contentDisposition == null || contentDisposition.isEmpty() ? null : extractFilename((String) contentDisposition.iterator().next());
    }

    @Nullable
    public static String extractFilename(String contentDisposition) {
        // HeaderUtil handles both filename and filename* parameters, prioritizing filename
        return HeaderUtil.extractQuotedValueFromHeaderWithEncoding(contentDisposition, "filename");
    }

    @SneakyThrows
    public static String generateContentDisposition(boolean attachment, String filename) {
        return (attachment ? "attachment" : "inline")
                + "; filename=\"" + filename.replace("\"", "\"\"")
                + "\"; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8);
    }
}
