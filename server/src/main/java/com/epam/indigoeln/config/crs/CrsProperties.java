/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.config.crs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("crs")
public class CrsProperties {

    private String searchServiceUrl;
    private String registrationServiceUrl;
    private String username;
    private String password;

    public String getSearchServiceUrl() {
        return searchServiceUrl;
    }

    public void setSearchServiceUrl(String searchServiceUrl) {
        this.searchServiceUrl = searchServiceUrl;
    }

    public String getRegistrationServiceUrl() {
        return registrationServiceUrl;
    }

    public void setRegistrationServiceUrl(String registrationServiceUrl) {
        this.registrationServiceUrl = registrationServiceUrl;
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
