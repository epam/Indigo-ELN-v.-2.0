package com.epam.indigoeln.config.crs;

import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

@Configuration
public class CrsConfiguration {

    @Autowired
    private CrsProperties crsProperties;

    @Bean
    public HttpInvokerProxyFactoryBean getSearch() {
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceUrl(crsProperties.getSearchServiceUrl());
        factory.setServiceInterface(BingoSearch.class);
        return factory;
    }

    @Bean
    public HttpInvokerProxyFactoryBean getRegistration() {
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceUrl(crsProperties.getRegistrationServiceUrl());
        factory.setServiceInterface(BingoRegistration.class);
        return factory;
    }


}
