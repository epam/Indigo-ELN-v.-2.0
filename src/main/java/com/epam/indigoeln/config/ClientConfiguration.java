package com.epam.indigoeln.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.*;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.StreamSupport;

@Component
public class ClientConfiguration implements InitializingBean {

    private static final String PREFIX = "client.";

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
        if (environment instanceof ConfigurableEnvironment){
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
