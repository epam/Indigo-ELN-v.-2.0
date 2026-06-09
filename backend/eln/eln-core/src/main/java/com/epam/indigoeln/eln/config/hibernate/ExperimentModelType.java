package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import io.quarkus.arc.Arc;
import org.jspecify.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExperimentModelType extends AbstractJsonUserType<ExperimentModel> {

    @Nullable
    private volatile SnapshotMapper snapshotMapper;

    ExperimentModelType() {
        super(ExperimentModel.class);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    @Nullable
    public ExperimentModel deepCopy(@Nullable ExperimentModel value) {
        return value != null ? getSnapshotMapper().copyModel(value) : null;
    }

    private SnapshotMapper getSnapshotMapper() {
        if (snapshotMapper == null) {
            synchronized(this) {
                if (snapshotMapper == null) {
                    snapshotMapper = Arc.container().instance(SnapshotMapper.class).get();
                }
            }
        }
        return checkNotNull(snapshotMapper);
    }
}
