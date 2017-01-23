package com.epam.indigoeln.config.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import com.epam.indigoeln.core.repository.bingo.BingoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class BingoConfig {

    @Value("${bingo.index-folder}")
    private String bingoFolder;

    @Autowired
    private IndigoConfig indigoConfig;

    @Bean
    public BingoRepository moleculeRepository() throws IOException {
        String type = BingoRepository.MOLECULE;

        Indigo indigo = indigoConfig.indigo();
        Bingo bingo = bingo(type, indigo);

        return new BingoRepository(type, bingo, indigo);
    }

    @Bean
    public BingoRepository reactionRepository() throws IOException {
        String type = BingoRepository.REACTION;

        Indigo indigo = indigoConfig.indigo();
        Bingo bingo = bingo(type, indigo);

        return new BingoRepository(type, bingo, indigo);
    }

    private Bingo bingo(String type, Indigo indigo) throws IOException {
        Bingo bingo;

        Path path = Paths.get(bingoFolder, type);

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
