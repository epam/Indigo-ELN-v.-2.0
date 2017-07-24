package com.epam.indigoeln.core.service.print.itext2.tests;

import com.epam.indigoeln.core.model.Experiment;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DummyExperimentProvider {

    private static String baseFolder = "C:/Users/Dmitrii_Naumenko/Desktop/dev/Indigo/test_jasper_report/";

    public static Experiment getExperiment() throws FileNotFoundException {
        return SerializationUtils.deserialize(new FileInputStream(baseFolder + "ex.ser"));
    }

}
