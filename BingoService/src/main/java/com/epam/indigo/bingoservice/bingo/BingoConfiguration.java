package com.epam.indigo.bingoservice.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class BingoConfiguration {

    @Value("${bingo.database.folder}/molecules")
    private String moleculeDatabaseFolder;

    @Value("${bingo.database.folder}/reactions")
    private String reactionDatabaseFolder;

    @Autowired
    private Indigo indigo;

    @Bean
    public Indigo indigo() {
        return new Indigo();
    }

    @Bean
    public Bingo moleculeDatabase() {
        return getDatabase(moleculeDatabaseFolder, "molecule");
    }

    @Bean
    public Bingo reactionDatabase() {
        return getDatabase(reactionDatabaseFolder, "reaction");
    }

    private Bingo getDatabase(String folder, String type) {
        Bingo bingo;

        File dir = new File(folder);

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                bingo = Bingo.createDatabaseFile(indigo, folder, type);
            } else {
                throw new RuntimeException("Cannot create directory structures for Bingo: " + folder);
            }
        } else {
            bingo = Bingo.loadDatabaseFile(indigo, folder);
        }

        bingo.optimize();

        return bingo;
    }
}
