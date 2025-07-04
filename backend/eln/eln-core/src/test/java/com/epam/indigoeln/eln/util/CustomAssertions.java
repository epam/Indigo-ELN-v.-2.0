package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AccessLevel;
import one.util.streamex.StreamEx;
import org.apache.http.HttpStatus;
import org.assertj.core.api.AbstractAssert;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CustomAssertions {

    public static ACLListAssert assertThatACL(List<ACLEntryDTO> actual) {
        return new ACLListAssert(actual);
    }

    public static <T> ClientCallAssert<T> assertThatClientCall(Runnable call) {
        return assertThatClientCall(() -> {
            call.run();
            return null;
        });
    }

    public static <T> ClientCallAssert<T> assertThatClientCall(Supplier<T> call) {
        return new ClientCallAssert<>(call);
    }

    public static class ACLListAssert extends AbstractAssert<ACLListAssert, List<ACLEntryDTO>> {

        protected ACLListAssert(List<ACLEntryDTO> actual) {
            super(actual, ACLListAssert.class);
        }

        public ACLListAssert containsOnly(String name, AccessLevel level, boolean inherited) {
            return containsOnly(Map.of(name, Pair.of(level, inherited)));
        }

        public ACLListAssert containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2) {
            return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2)));
        }

        public ACLListAssert containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2, String name3, AccessLevel level3, boolean inherited3) {
            return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2), name3, Pair.of(level3, inherited3)));
        }

        public ACLListAssert containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2, String name3, AccessLevel level3, boolean inherited3, String name4, AccessLevel level4, boolean inherited4) {
            return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2), name3, Pair.of(level3, inherited3), name4, Pair.of(level4, inherited4)));
        }

        public ACLListAssert containsOnly(Map<String, Pair<AccessLevel, Boolean>> expected) {
            Map<String, Pair<AccessLevel, Boolean>> actualMap = StreamEx.of(actual).toMap(ACLEntryDTO::getDisplayName, e -> Pair.of(e.getLevel(), e.getInherited()));
            assertThat(actualMap).containsExactlyInAnyOrderEntriesOf(expected);
            return this;
        }
    }

    public static class ClientCallAssert<T> extends AbstractAssert<ClientCallAssert<T>, Supplier<T>> {

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

        public ClientCallAssert<T> isAllowedIf(boolean condition, String messagePattern, String... messagePatterns) {
            if (condition) {
                return isSuccessful();
            } else {
                return isForbidden(messagePattern, messagePatterns);
            }
        }
    }
}
