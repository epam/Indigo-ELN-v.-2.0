package com.epam.indigo.bingodb.config;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class BingoConfiguration {

    @Autowired
    private BingoProperties properties;

    public BingoConfiguration() {
        System.setProperty("jna.nosys", "true");
    }

    @Bean
    public Indigo indigo() {
        return new Indigo();
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
