package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.model.ExperimentStatus;
import one.util.streamex.StreamEx;

import java.util.Map;

public class ExperimentCountArrayType extends AbstractReadOnlyArrayType<Map<ExperimentStatus, Integer>> {

    @Override
    public Class<Map<ExperimentStatus, Integer>> returnedClass() {
        //noinspection unchecked,rawtypes
        return (Class) Map.class;
    }

    @Override
    protected Map<ExperimentStatus, Integer> doRead(StreamEx<String[]> stream) {
        return stream.toMap(a -> ExperimentStatus.valueOf(a[0]), a -> Integer.parseInt(a[1]));
    }
}
