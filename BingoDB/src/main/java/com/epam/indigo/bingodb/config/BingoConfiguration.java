package com.epam.indigo.bingodb.config;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.File;

@Configuration
public class BingoConfiguration {

    private static String LABEL_MODE = "hetero";
    private static String PICTURE_FORMAT = "svg";
    private static double BOND_LENGTH = -1d;
    private static boolean COLOR = true;
    private static boolean STEREOCHEM_ERRORS = true;

    @Autowired
    private BingoProperties properties;

    public BingoConfiguration() {
        System.setProperty("jna.nosys", "true");
    }

    @Bean
    public Indigo indigo() {
        Indigo indigo = new Indigo();

        indigo.setOption("ignore-stereochemistry-errors", STEREOCHEM_ERRORS);

        return indigo;
    }

    @Bean
    public IndigoRenderer renderer() {

        Indigo indigo = indigo();
        IndigoRenderer renderer = new IndigoRenderer(indigo());

        indigo.setOption("render-bond-length", BOND_LENGTH);
        indigo.setOption("render-label-mode", LABEL_MODE);
        indigo.setOption("render-output-format", PICTURE_FORMAT);
        indigo.setOption("render-coloring", COLOR);

        return renderer;
    }

    @Bean
    public Bingo moleculeDatabase() {
        return getDatabase(properties.getDatabaseFolder() + "/molecule", "molecule");
    }

    @Bean
    public Bingo reactionDatabase() {
        return getDatabase(properties.getDatabaseFolder() + "/reaction", "reaction");
    }

    private Bingo getDatabase(String folder, String type) {
        Bingo bingo;

        File dir = new File(folder);

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                bingo = Bingo.createDatabaseFile(indigo(), folder, type);
            } else {
                throw new RuntimeException("Cannot create directory structure for Bingo: " + folder);
            }
        } else {
            bingo = Bingo.loadDatabaseFile(indigo(), folder);
        }

        bingo.optimize();

        return bingo;
    }
}
