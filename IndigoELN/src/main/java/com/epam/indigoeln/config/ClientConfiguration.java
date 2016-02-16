package com.epam.indigoeln.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class ClientConfiguration implements InitializingBean {

    private final static String PREFIX = "client.";

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
            Iterator<PropertySource<?>> iterator = ((ConfigurableEnvironment) environment).getPropertySources().iterator();
            while (iterator.hasNext()) {
                PropertySource<?> propertySource = iterator.next();
                if (propertySource instanceof EnumerablePropertySource) {
                    String[] propertyNamesArray = ((EnumerablePropertySource) propertySource).getPropertyNames();
                    for (String propertyName : propertyNamesArray) {
                        if (propertyName.startsWith(PREFIX)) {
                            propertyNames.add(propertyName);
                        }
                    }
                }
            }
        }
        return propertyNames;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
