package com.epam.indigoeln.reaction.model.patch.handler2;

import org.apache.commons.math3.util.Precision;

public class FloatDiffHandler extends DefaultDiffHandler<Double> {

    public static final FloatDiffHandler INSTANCE = new FloatDiffHandler();

    private static final double EPSILON = 1e-6;

    public FloatDiffHandler() {
        super(null);
    }

    @Override
    protected boolean doEquals(Double a, Double b) {
        return Precision.equalsWithRelativeTolerance(a, b, EPSILON);
    }
}
