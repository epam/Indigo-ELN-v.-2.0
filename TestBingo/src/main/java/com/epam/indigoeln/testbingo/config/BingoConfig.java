package com.epam.indigoeln.testbingo.config;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class BingoConfig {

    @Value("${bingo.index-folder}")
    private String indexFolder;

    public BingoConfig() {
        System.setProperty("jna.nosys", "true");
    }

    @Bean
    public String molFile() throws IOException {
        try (InputStream is = BingoConfig.class.getClassLoader().getResourceAsStream("chem/benzene.mol")) {
            return IOUtils.toString(is);
        }
    }

    @Bean
    public String rxnFile() throws IOException {
        try (InputStream is = BingoConfig.class.getClassLoader().getResourceAsStream("chem/benzene.rxn")) {
            return IOUtils.toString(is);
        }
    }

    @Bean
    public Indigo indigo() {
        return new Indigo();
    }

    @Bean
    public Bingo moleculeBingo() {
        return getDatabase(indexFolder + "/molecule", "molecule");
    }

    @Bean
    public Bingo reactionBingo() {
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
