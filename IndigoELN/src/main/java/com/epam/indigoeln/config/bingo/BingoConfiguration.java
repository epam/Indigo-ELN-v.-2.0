package com.epam.indigoeln.config.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class BingoConfiguration {

    @Value("${bingo.index-folder}")
    private String indexFolder;

    public BingoConfiguration() {
        System.setProperty("jna.nosys", "true");
    }

    @Bean
    public Indigo indigo() {
        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }

    @Bean
    public IndigoRenderer indigoRenderer() {
        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        IndigoRenderer renderer = new IndigoRenderer(indigo);

        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);

        return renderer;
    }

    @Bean
    public Bingo moleculeDatabase() {
        return getDatabase(indexFolder + "/molecule", "molecule");
    }

    @Bean
    public Bingo reactionDatabase() {
        return getDatabase(indexFolder + "/reaction", "reaction");
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
