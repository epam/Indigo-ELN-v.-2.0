package com.epam.indigoeln.compound.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class STRCodeCompound {

    private static final Pattern PATTERN = Pattern.compile("STR-(\\d{8})-(\\d{2})");

    int compoundCode;
    int saltCode;

    public STRCodeCompound(int compoundCode, int saltCode) {
        this.compoundCode = compoundCode;
        this.saltCode = saltCode;
    }

    public static STRCodeCompound parse(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid STR code format: " + str);
        }
        return new STRCodeCompound(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    @Override
    public String toString() {
        return "STR-%08d-%02d".formatted(compoundCode, saltCode);
    }
}
