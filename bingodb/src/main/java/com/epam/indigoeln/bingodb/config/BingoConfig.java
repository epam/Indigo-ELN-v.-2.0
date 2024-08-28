/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of BingoDB.
 *
 *  BingoDB is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BingoDB is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Configuration for Bingo library for molecule/reaction databases.
 * <p>
 * According to Bingo library restrictions:
 * <p>
 * - Bingo works with molecules and reactions in their own database files
 * <p>
 * - Each Bingo instance should use own Indigo instance
 * <p>
 * - All structure loading/parsing should be processed with these Indigo instances
 */
@Configuration
public class BingoConfig {

    /**
     * Configuration properties for databases.
     */
    private final BingoProperties bingoProperties;

    /**
     * Indigo instance for molecule database Bingo instance.
     */
    private final Indigo moleculeIndigo;

    /**
     * Indigo instance for reaction database Bingo instance.
     */
    private final Indigo reactionIndigo;

    /**
     * Create a new BingoConfig instance.
     *
     * @param bingoProperties Configuration properties for databases
     * @param moleculeIndigo  Indigo instance for molecule database Bingo instance
     * @param reactionIndigo  Indigo instance for reaction database Bingo instance
     */
    @Autowired
    public BingoConfig(BingoProperties bingoProperties,
                       Indigo moleculeIndigo,
                       Indigo reactionIndigo) {
        this.bingoProperties = bingoProperties;
        this.moleculeIndigo = moleculeIndigo;
        this.reactionIndigo = reactionIndigo;
    }

    /**
     * Create a new Bingo instance for molecule database.
     *
     * @return Bingo instance for molecule database
     * @throws IOException if database creation failed
     */
    @Bean
    public Bingo moleculeBingo() throws IOException {
        return bingo("molecule", moleculeIndigo);
    }

    /**
     * Create a new Bingo instance for reaction database.
     *
     * @return Bingo instance for reaction database
     * @throws IOException if database creation failed
     */
    @Bean
    public Bingo reactionBingo() throws IOException {
        return bingo("reaction", reactionIndigo);
    }

    /**
     * Create a new Bingo instance for specified structure type.
     *
     * @param type   structure type - molecule or reaction
     * @param indigo Indigo instance to use
     * @return Bingo instance for specified structure type
     * @throws IOException if database creation failed
     */
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
