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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class CrsConfiguration {

    @Autowired
    private CrsProperties crsProperties;

    @Bean
    public BingoSearchClient getSearch() {
        System.err.println("!!! crsProperties.getSearchServiceUrl = " + crsProperties.getSearchServiceUrl());
        System.err.println("!!! crsProperties.getRegistrationServiceUrl = " + crsProperties.getRegistrationServiceUrl());
        System.err.println("!!! crsProperties.getUsername = " + crsProperties.getUsername());
        System.err.println("!!! crsProperties.getPassword = " + crsProperties.getPassword());
        RestClient restClient = RestClient.builder().baseUrl(crsProperties.getSearchServiceUrl()).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(BingoSearchClient.class);
    }

    @Bean
    public BingoRegistrationClient getRegistration() {
        RestClient restClient = RestClient.builder().baseUrl(crsProperties.getRegistrationServiceUrl()).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(BingoRegistrationClient.class);
    }

}
