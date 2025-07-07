package com.epam.indigoeln.compound.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class STRCode {

    private static final Pattern COMPOUND_PATTERN = Pattern.compile("STR-(\\d{8})-(\\d{2})(-(\\d{3}))?");

    int compoundCode;
    int saltCode;
    @Nullable
    Integer sampleCode;

    public static STRCode parse(String str) {
        Matcher matcher = COMPOUND_PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid STR code format: " + str);
        }
        return new STRCode(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : null);
    }

    @Override
    public String toString() {
        if (sampleCode != null) {
            return "STR-%08d-%02d-%03d".formatted(compoundCode, saltCode, sampleCode);
        } else {
            return "STR-%08d-%02d".formatted(compoundCode, saltCode);
        }
    }
}
