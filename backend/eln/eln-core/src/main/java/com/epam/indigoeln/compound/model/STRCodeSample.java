package com.epam.indigoeln.compound.model;

import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class STRCodeSample {

    private static final Pattern PATTERN = Pattern.compile("STR-(\\d{8})-(\\d{2})-(\\d{3})");

    int compoundCode;
    int saltCode;
    int sampleCode;

    public static STRCodeSample parse(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid STR code format: " + str);
        }
        return new STRCodeSample(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    @Override
    public String toString() {
        return "STR-%08d-%02d-%03d".formatted(compoundCode, saltCode, sampleCode);
    }
}
