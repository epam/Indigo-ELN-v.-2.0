package com.epam.indigoeln.core.service.print.itext2.tests;

import com.lowagie.text.DocumentException;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGeneratorRunner {

    private static final String BASE_PATH = "" +
            "C:/Users/Dmitrii_Naumenko/Desktop/dev" +
            "/Indigo/indigo-eln/IndigoELN/src/main/java" +
            "/com/epam/indigoeln/core/service/print/itext2/";


    public static void main(String[] args) throws IOException, DocumentException {
        String fileName = BASE_PATH + "sample.pdf";
        File file = new File(fileName);

        try (FileOutputStream output = new FileOutputStream(file)) {
//            Experiment experiment = DummyExperimentProvider.getExperiment();
//            PdfGenerator pdfGenerator = new PdfGenerator(experiment);
//            pdfGenerator.generate(output);

            Desktop.getDesktop().open(file);
        }
    }

}
