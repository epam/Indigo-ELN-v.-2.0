package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import one.util.streamex.StreamEx;

import java.util.Map;

public class ExperimentCountArrayType extends AbstractArrayOfStructType<Map<ExperimentStatus, Integer>, Pair<ExperimentStatus, Integer>> {

    @Override
    public Class<Map<ExperimentStatus, Integer>> returnedClass() {
        //noinspection unchecked,rawtypes
        return (Class) Map.class;
    }

    @Override
    protected Pair<ExperimentStatus, Integer> doRead(String[] parts) {
        return Pair.of(ExperimentStatus.valueOf(parts[0]), Integer.valueOf(parts[1]));
    }

    @Override
    protected Map<ExperimentStatus, Integer> doAssemble(StreamEx<Pair<ExperimentStatus, Integer>> stream) {
        return stream.toMap(Pair::a, Pair::b);
    }

    @Override
    protected String getSQLElementType() {
        return "Experiment_Count";
    }
}
