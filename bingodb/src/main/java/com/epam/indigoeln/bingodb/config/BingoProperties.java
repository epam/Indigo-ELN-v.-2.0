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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Bingo databases.
 */
@Configuration
@ConfigurationProperties("bingo")
public class BingoProperties {

    /**
     * Path to folder with molecule/reaction databases.
     */
    private String folder;

    private String username;
    private String password;

    /**
     * Get an existing folder value.
     *
     * @return existing folder value
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Set a new folder value.
     *
     * @param folder new folder value to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
