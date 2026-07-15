package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MolFormula {

    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z]+)(<sub>)?([0-9]*)(</sub>)?\\s*");

    private final String[] elements;
    private final int[] counts;

    public static String normalize(String formula) {
        return formula.replace(" ", "");
    }

    @JsonCreator
    public MolFormula(String formula) {
        Matcher matcher = PATTERN.matcher(formula);
        String[] elements = new String[formula.length()];
        int[] counts = new int[formula.length()];
        int count = 0;
        while (matcher.find()) {
            elements[count] = matcher.group(1);
            counts[count] = !matcher.group(3).isEmpty() ? Integer.parseInt(matcher.group(3)) : 1;
            count++;
        }
        Preconditions.checkArgument(count != 0, "Invalid formula: %s", formula);
        this.elements = Arrays.copyOf(elements, count);
        this.counts = Arrays.copyOf(counts, count);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MolFormula that)) return false;
        return Arrays.equals(elements, that.elements) && Arrays.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(elements), Arrays.hashCode(counts));
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            s.append(elements[i]);
            if (counts[i] != 1) {
                s.append(counts[i]);
            }
        }
        return s.toString();
    }

    @JsonValue
    public String toHTMLString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            s.append(elements[i]);
            if (counts[i] != 1) {
                s.append("<sub>").append(counts[i]).append("</sub>");
            }
        }
        return s.toString();
    }
}
