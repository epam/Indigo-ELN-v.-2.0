package com.epam.indigoeln.reaction.model;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class Anchor {

    private static final String SHORT_FORMAT = "[A-Za-z0-9_-]{8}";

    private static final String LEGACY_UUID_FORMAT = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    // TODO accept the short form only once no stored model, mutation or diff carries a UUID anchor
    // private static final Pattern FORMAT = Pattern.compile(SHORT_FORMAT);
    private static final Pattern FORMAT = Pattern.compile(SHORT_FORMAT + "|" + LEGACY_UUID_FORMAT);

    @Getter
    private final String value;

    protected Anchor(String str) {
        checkArgument(FORMAT.matcher(str).matches(), "Invalid anchor: %s", str);
        this.value = str;
    }

    // 48 random bits as 8 base64url chars; an anchor only has to be unique within its experiment
    protected static String generate() {
        byte[] bytes = new byte[6];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof Anchor anchor)) return false;
        return value.equals(anchor.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
