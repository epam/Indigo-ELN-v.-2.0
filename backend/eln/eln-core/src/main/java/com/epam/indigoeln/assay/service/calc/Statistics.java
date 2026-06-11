package com.epam.indigoeln.assay.service.calc;

/** Small numeric helpers shared by the built-in calculation providers. */
final class Statistics {

    private Statistics() {
    }

    static double mean(double[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("mean requires at least one value");
        }
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** Sample standard deviation (n-1 denominator). */
    static double stdDev(double[] values) {
        if (values.length < 2) {
            return 0;
        }
        double mean = mean(values);
        double sumSq = 0;
        for (double v : values) {
            double d = v - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    static double cv(double[] values) {
        double mean = mean(values);
        if (mean == 0) {
            throw new ArithmeticException("Coefficient of variation is undefined when the mean is zero");
        }
        return stdDev(values) / mean * 100.0;
    }
}
