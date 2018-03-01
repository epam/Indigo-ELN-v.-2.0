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
package com.epam.indigoeln.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.StreamSupport;

@Component
public class ClientConfiguration implements InitializingBean {

    private static final String PREFIX = "indigoeln.client.";

    @Autowired
    private Environment environment;

    private Map<String, String> properties;


    @Override
    public void afterPropertiesSet() throws Exception {
        Set<String> propertyNames = getPropertyNames();
        properties = new HashMap<>();
        for (String propertyName : propertyNames) {
            properties.put(propertyName, environment.getProperty(propertyName));
        }
    }

    private Set<String> getPropertyNames() {
        Set<String> propertyNames = new HashSet<>();
        if (environment instanceof ConfigurableEnvironment) {
            MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
            StreamSupport.stream(propertySources.spliterator(), false)
                    .filter(p -> p instanceof EnumerablePropertySource)
                    .flatMap(p -> Arrays.stream(((EnumerablePropertySource) p).getPropertyNames()))
                    .filter(s -> s.startsWith(PREFIX))
                    .forEach(propertyNames::add);
        }
        return propertyNames;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
