package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.eln.model.ExperimentRef;

public class ExperimentRefDiffHandler extends DefaultDiffHandler<ExperimentRef> {

    public static final ExperimentRefDiffHandler INSTANCE = new ExperimentRefDiffHandler();

    public ExperimentRefDiffHandler() {
        super(null);
    }

    @Override
    protected boolean doEquals(ExperimentRef a, ExperimentRef b) {
        return a.equals(b);
    }
}
