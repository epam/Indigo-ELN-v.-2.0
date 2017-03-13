package com.epam.indigoeln.config.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class BingoConfig {

    @Autowired
    private BingoProperties bingoProperties;

    @Autowired
    private Indigo moleculeIndigo;

    @Autowired
    private Indigo reactionIndigo;

    @Bean
    public Bingo moleculeBingo() throws IOException {
        return bingo("molecule", moleculeIndigo);
    }

    @Bean
    public Bingo reactionBingo() throws IOException {
        return bingo("reaction", reactionIndigo);
    }

    private Bingo bingo(String type, Indigo indigo) throws IOException {
        Bingo bingo;

        Path path = Paths.get(bingoProperties.getIndexFolder(), type);

        if (Files.exists(path)) {
            bingo = Bingo.loadDatabaseFile(indigo, path.toString());
        } else {
            Files.createDirectories(path);

            bingo = Bingo.createDatabaseFile(indigo, path.toString(), type);
        }

        bingo.optimize();

        return bingo;
    }
}
