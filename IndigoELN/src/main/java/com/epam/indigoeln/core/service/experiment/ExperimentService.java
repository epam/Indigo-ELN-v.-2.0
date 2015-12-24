package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Experiment;

import java.util.stream.Stream;

public interface ExperimentService {

    Stream<Experiment> getExperiments();
}
