package com.epam.indigoeln.config;

import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

@Configuration
public class CrsConfiguration {

    @Value("${crs.searchservice.url}")
    private String searchServiceUrl;

    @Value("${crs.registrationservice.url}")
    private String registrationServiceUrl;

    @Bean
    public HttpInvokerProxyFactoryBean getSearch() {
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceUrl(searchServiceUrl);
        factory.setServiceInterface(BingoSearch.class);
        return factory;
    }

    @Bean
    public HttpInvokerProxyFactoryBean getRegistration() {
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceUrl(registrationServiceUrl);
        factory.setServiceInterface(BingoRegistration.class);
        return factory;
    }


}
