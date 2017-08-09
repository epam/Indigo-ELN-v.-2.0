package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class BingoConfig {

    private final BingoProperties bingoProperties;

    private final Indigo moleculeIndigo;
    private final Indigo reactionIndigo;

    @Autowired
    public BingoConfig(BingoProperties bingoProperties,
                       Indigo moleculeIndigo,
                       Indigo reactionIndigo) {
        this.bingoProperties = bingoProperties;
        this.moleculeIndigo = moleculeIndigo;
        this.reactionIndigo = reactionIndigo;
    }

    @Bean
    public Bingo moleculeBingo() throws IOException {
        return bingo("molecule", moleculeIndigo);
    }

    @Bean
    public Bingo reactionBingo() throws IOException {
        return bingo("reaction", reactionIndigo);
    }

    private Bingo bingo(String type, Indigo indigo) throws IOException {
        String folder = bingoProperties.getFolder() + File.separator + type;

        Bingo bingo;

        if (Files.exists(Paths.get(folder))) {
            bingo = Bingo.loadDatabaseFile(indigo, folder);
        } else {
            Files.createDirectories(Paths.get(folder));
            bingo = Bingo.createDatabaseFile(indigo, folder, type);
        }

        bingo.optimize();

        return bingo;
    }
}
