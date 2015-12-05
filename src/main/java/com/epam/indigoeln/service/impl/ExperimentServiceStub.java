package com.epam.indigoeln.service.impl;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.epam.indigoeln.service.Experiment;
import com.epam.indigoeln.service.ExperimentService;

@Component
public class ExperimentServiceStub implements ExperimentService {
	private class ExperimentStub implements Experiment {
		private String name;
		
		public ExperimentStub(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
	
	@Override
	public Stream<Experiment> getExperiments() {
		Experiment[] result = { new ExperimentStub("Experiment 1"), new ExperimentStub("Experiment 2"), new ExperimentStub("Experiment 3")};
		
		return Arrays.stream(result);
	}
}
