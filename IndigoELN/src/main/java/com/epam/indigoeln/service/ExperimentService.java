package com.epam.indigoeln.service;

import java.util.stream.Stream;

public interface ExperimentService {
	Stream<Experiment> getExperiments();
}
