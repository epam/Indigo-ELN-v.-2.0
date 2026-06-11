package com.epam.indigoeln.assay.service.calc;

import java.util.Map;

/**
 * A small, sandboxed arithmetic expression evaluator for user-authored derived-value formulas.
 * It is deliberately <em>not</em> a general scripting engine: it supports only numeric literals,
 * named inputs, the operators {@code + - * / ^}, parentheses, and a fixed whitelist of functions.
 * There is no access to variables, methods, I/O or types beyond what is listed here, so an
 * untrusted formula cannot reach outside the calculation.
 *
 * <p>Named inputs are vectors (a single well is a length-1 vector). Arithmetic operators and the
 * scalar math functions require length-1 operands; the aggregation functions accept any length.
 *
 * <p>Supported functions:
 * <ul>
 *   <li>aggregations: {@code mean(x) sum(x) min(x) max(x) sd(x) count(x)}</li>
 *   <li>scalar math: {@code abs(x) sqrt(x) ln(x) log10(x) exp(x) pow(a,b)}</li>
 * </ul>
 */
public final class FormulaEvaluator {

    private final String src;
    private final Map<String, double[]> inputs;
    private int pos;

    private FormulaEvaluator(String src, Map<String, double[]> inputs) {
        this.src = src;
        this.inputs = inputs;
    }

    /** Parses and evaluates {@code expression} against the given named inputs. */
    public static double evaluate(String expression, Map<String, double[]> inputs) {
        FormulaEvaluator e = new FormulaEvaluator(expression, inputs);
        double[] result = e.parseAndCheckComplete();
        return scalar(result, "result");
    }

    /** Parses {@code expression} for validity (does not evaluate against real data). */
    public static void validate(String expression) {
        // Resolve any input name to a neutral value so syntax, function names and arity are
        // checked without needing actual data.
        new FormulaEvaluator(expression, Map.of()).parseValidationOnly();
    }

    private void parseValidationOnly() {
        pos = 0;
        validating = true;
        expr();
        skipWs();
        if (pos < src.length()) {
            throw new FormulaException("Unexpected character at position " + pos + " in formula");
        }
    }

    private boolean validating;

    private double[] parseAndCheckComplete() {
        pos = 0;
        double[] v = expr();
        skipWs();
        if (pos < src.length()) {
            throw new FormulaException("Unexpected character '" + src.charAt(pos) + "' at position " + pos);
        }
        return v;
    }

    // --- recursive descent -------------------------------------------------

    private double[] expr() {
        double[] value = term();
        while (true) {
            skipWs();
            char c = peek();
            if (c == '+') {
                pos++;
                value = scalarResult(scalar(value, "+") + scalar(term(), "+"));
            } else if (c == '-') {
                pos++;
                value = scalarResult(scalar(value, "-") - scalar(term(), "-"));
            } else {
                return value;
            }
        }
    }

    private double[] term() {
        double[] value = power();
        while (true) {
            skipWs();
            char c = peek();
            if (c == '*') {
                pos++;
                value = scalarResult(scalar(value, "*") * scalar(power(), "*"));
            } else if (c == '/') {
                pos++;
                double divisor = scalar(power(), "/");
                if (!validating && divisor == 0) {
                    throw new FormulaException("Division by zero in formula");
                }
                value = scalarResult(scalar(value, "/") / divisor);
            } else {
                return value;
            }
        }
    }

    private double[] power() {
        double[] base = unary();
        skipWs();
        if (peek() == '^') {
            pos++;
            double exponent = scalar(power(), "^");
            return scalarResult(Math.pow(scalar(base, "^"), exponent));
        }
        return base;
    }

    private double[] unary() {
        skipWs();
        char c = peek();
        if (c == '-') {
            pos++;
            return scalarResult(-scalar(unary(), "-"));
        }
        if (c == '+') {
            pos++;
            return unary();
        }
        return primary();
    }

    private double[] primary() {
        skipWs();
        char c = peek();
        if (c == '(') {
            pos++;
            double[] v = expr();
            expect(')');
            return v;
        }
        if (Character.isDigit(c) || c == '.') {
            return scalarResult(number());
        }
        if (Character.isLetter(c) || c == '_') {
            return identifier();
        }
        throw new FormulaException("Unexpected character '" + c + "' at position " + pos);
    }

    private double[] identifier() {
        int start = pos;
        while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) {
            pos++;
        }
        String name = src.substring(start, pos);
        skipWs();
        if (peek() == '(') {
            pos++;
            return callFunction(name);
        }
        return resolve(name);
    }

    private double[] callFunction(String name) {
        java.util.List<double[]> args = new java.util.ArrayList<>();
        skipWs();
        if (peek() != ')') {
            args.add(expr());
            skipWs();
            while (peek() == ',') {
                pos++;
                args.add(expr());
                skipWs();
            }
        }
        expect(')');
        return scalarResult(applyFunction(name, args));
    }

    private double applyFunction(String name, java.util.List<double[]> args) {
        switch (name) {
            case "mean": return Statistics.mean(arg(args, 0, name));
            case "sd": return Statistics.stdDev(arg(args, 0, name));
            case "sum": { double s = 0; for (double v : arg(args, 0, name)) s += v; return s; }
            case "min": { double m = Double.POSITIVE_INFINITY; for (double v : arg(args, 0, name)) m = Math.min(m, v); return m; }
            case "max": { double m = Double.NEGATIVE_INFINITY; for (double v : arg(args, 0, name)) m = Math.max(m, v); return m; }
            case "count": return arg(args, 0, name).length;
            case "abs": return Math.abs(scalar(arg(args, 0, name), name));
            case "sqrt": return Math.sqrt(scalar(arg(args, 0, name), name));
            case "ln": return Math.log(scalar(arg(args, 0, name), name));
            case "log10": return Math.log10(scalar(arg(args, 0, name), name));
            case "exp": return Math.exp(scalar(arg(args, 0, name), name));
            case "pow":
                requireArity(args, 2, name);
                return Math.pow(scalar(args.get(0), name), scalar(args.get(1), name));
            default:
                throw new FormulaException("Unknown function '" + name + "' in formula");
        }
    }

    private double[] arg(java.util.List<double[]> args, int index, String fn) {
        requireArity(args, 1, fn);
        return args.get(index);
    }

    private void requireArity(java.util.List<double[]> args, int n, String fn) {
        if (args.size() != n) {
            throw new FormulaException("Function '" + fn + "' expects " + n + " argument(s) but got " + args.size());
        }
    }

    private double[] resolve(String name) {
        double[] values = inputs.get(name);
        if (values == null) {
            if (validating) {
                return new double[]{1.0};
            }
            throw new FormulaException("Unknown input '" + name + "' in formula");
        }
        return values;
    }

    private double number() {
        int start = pos;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.'
                || src.charAt(pos) == 'e' || src.charAt(pos) == 'E'
                || ((src.charAt(pos) == '+' || src.charAt(pos) == '-') && pos > start
                    && (src.charAt(pos - 1) == 'e' || src.charAt(pos - 1) == 'E')))) {
            pos++;
        }
        try {
            return Double.parseDouble(src.substring(start, pos));
        } catch (NumberFormatException ex) {
            throw new FormulaException("Invalid number at position " + start);
        }
    }

    private static double[] scalarResult(double v) {
        return new double[]{v};
    }

    private static double scalar(double[] value, String op) {
        if (value.length != 1) {
            throw new FormulaException("Operator/function '" + op + "' requires a single value but received a vector of length " + value.length);
        }
        return value[0];
    }

    private char peek() {
        return pos < src.length() ? src.charAt(pos) : '\0';
    }

    private void expect(char c) {
        skipWs();
        if (peek() != c) {
            throw new FormulaException("Expected '" + c + "' at position " + pos);
        }
        pos++;
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }
}
