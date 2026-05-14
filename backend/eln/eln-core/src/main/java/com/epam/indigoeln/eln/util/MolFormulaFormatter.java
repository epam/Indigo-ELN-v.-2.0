package com.epam.indigoeln.eln.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MolFormulaFormatter {

    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z]+)([0-9]*)\\s*");

    public static String format(String molecularFormula) {
        var matcher = PATTERN.matcher(molecularFormula);
        var sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)));
            if (!matcher.group(2).isEmpty()) {
                sb.append("<sub>").append(Matcher.quoteReplacement(matcher.group(2))).append("</sub>");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
