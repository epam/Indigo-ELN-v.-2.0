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

import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Indigo instances beans.
 */
@Configuration
public class IndigoConfig {

    /**
     * IndigoProvider instance which provides Indigo instances with common properties.
     */
    private final IndigoProvider indigoProvider;

    /**
     * Create a new IndigoConfig instance.
     *
     * @param indigoProvider IndigoProvider instance
     */
    @Autowired
    public IndigoConfig(IndigoProvider indigoProvider) {
        this.indigoProvider = indigoProvider;
    }

    /**
     * Create a new Indigo instance for molecule database Bingo instance.
     *
     * @return Indigo instance for reaction database Bingo instance
     */
    @Bean
    public Indigo moleculeIndigo() {
        return indigoProvider.indigo();
    }

    /**
     * Create a new Indigo instance for reaction database Bingo instance.
     *
     * @return Indigo instance for reaction database Bingo instance
     */
    @Bean
    public Indigo reactionIndigo() {
        return indigoProvider.indigo();
    }
}
