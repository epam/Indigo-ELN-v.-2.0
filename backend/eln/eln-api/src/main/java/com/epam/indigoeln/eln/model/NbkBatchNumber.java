package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class NbkBatchNumber {

    private static final Pattern PATTERN = Pattern.compile("(\\d{8}-\\d{4})-(\\d{3})");

    String experimentName;
    int ordinal;

    @JsonCreator
    public static NbkBatchNumber parse(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid nbk batch number format: " + str);
        }
        return new NbkBatchNumber(matcher.group(1), Integer.parseInt(matcher.group(2)));
    }

    @JsonValue
    String getStringForm() {
        return toString();
    }

    @Override
    public String toString() {
        return "%s-%03d".formatted(experimentName, ordinal);
    }
}
