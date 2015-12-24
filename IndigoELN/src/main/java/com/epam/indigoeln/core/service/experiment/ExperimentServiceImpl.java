package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Experiment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Stream;

@Service
public class ExperimentServiceImpl implements ExperimentService {

    @Override
    public Stream<Experiment> getExperiments() {
        Experiment[] result = {new Experiment("Experiment 1"), new Experiment("Experiment 2"), new Experiment("Experiment 3")};

        return Arrays.stream(result);
    }
}
