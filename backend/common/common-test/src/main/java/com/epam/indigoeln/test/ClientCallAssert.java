package com.epam.indigoeln.test;

import one.util.streamex.StreamEx;
import org.apache.http.HttpStatus;
import org.assertj.core.api.AbstractAssert;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClientCallAssert<T> extends AbstractAssert<ClientCallAssert<T>, Supplier<T>> {

    public static <T> ClientCallAssert<T> assertThatClientCall(Runnable call) {
        return assertThatClientCall(() -> {
            call.run();
            return null;
        });
    }

    public static <T> ClientCallAssert<T> assertThatClientCall(Supplier<T> call) {
        return new ClientCallAssert<>(call);
    }

    private final @Nullable T result;
    private final @Nullable Throwable exception;

    protected ClientCallAssert(Supplier<T> actual) {
        super(actual, ClientCallAssert.class);
        T result0 = null;
        Throwable exception0 = null;
        try {
            result0 = actual.get();
        } catch (Throwable e) {
            exception0 = e;
        }
        result = result0;
        exception = exception0;
    }

    public ClientCallAssert<T> isSuccessful() {
        assertThat(exception).as(descriptionText() + "\nCall expected to succeed, but failed").isNull();
        return this;
    }

    public ClientCallAssert<T> isSuccessfulWithResult(Consumer<T> consumer) {
        assertThat(exception).as(descriptionText() + "\nCall expected to succeed, but failed").isNull();
        consumer.accept(result);
        return this;
    }

    public ClientCallAssert<T> isFailedWithStatusCode(int statusCode, String messagePattern, String... messagePatterns) {
        assertThat(exception).as(descriptionText() + "\nCall expected to fail with code %s, but succeeded", statusCode).isNotNull();
        assertThat(exception).isInstanceOfSatisfying(APICallException.class, e -> {
            assertThat(e.getStatusCode()).as(descriptionText() + "\nCall failed with wrong code").isEqualTo(statusCode);
            List<String> allPatterns = StreamEx.of(messagePatterns).prepend(messagePattern).toList();
            for (String pattern : allPatterns) {
                Pattern pt = Pattern.compile(pattern);
                if (e.getErrors().stream().noneMatch(err -> pt.matcher(err.getMessage()).find())) {
                    fail("Expected error messages to contain pattern %s:\n\n%s", pattern, StreamEx.of(e.getErrors()).joining("\n"));
                }
            }
        });
        return this;
    }

    public ClientCallAssert<T> isBadRequest(String messagePattern, String... messagePatterns) {
        return isFailedWithStatusCode(HttpStatus.SC_BAD_REQUEST, messagePattern, messagePatterns);
    }

    public ClientCallAssert<T> isForbidden(String messagePattern, String... messagePatterns) {
        return isFailedWithStatusCode(HttpStatus.SC_FORBIDDEN, messagePattern, messagePatterns);
    }

    public ClientCallAssert<T> isNotFound(String messagePattern, String... messagePatterns) {
        return isFailedWithStatusCode(HttpStatus.SC_NOT_FOUND, messagePattern, messagePatterns);
    }

    public ClientCallAssert<T> isConflict(String messagePattern, String... messagePatterns) {
        return isFailedWithStatusCode(HttpStatus.SC_CONFLICT, messagePattern, messagePatterns);
    }

    public ClientCallAssert<T> isAllowedIf(boolean condition, String messagePattern, String... messagePatterns) {
        if (condition) {
            return isSuccessful();
        } else {
            return isForbidden(messagePattern, messagePatterns);
        }
    }
}
