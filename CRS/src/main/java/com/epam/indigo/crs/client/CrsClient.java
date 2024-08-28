/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of CRS.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.epam.indigo.crs.client;

import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CrsClient {

    private static final String CLIENT_CONTEXT_FILE = "xml/crs-client-context.xml";

    private static final Object lock = new Object();

    private static Map<String, Object> cache = new HashMap<String, Object>();

    public static BingoRegistration getRegistrationService(String serviceUrl) {
        return (BingoRegistration) getService(serviceUrl, "RegistrationService", BingoRegistration.class);
    }

    public static BingoSearch getSearchService(String serviceUrl) {
        return (BingoSearch) getService(serviceUrl, "SearchService", BingoSearch.class);
    }

    private static Object getService(String serviceUrl, String serviceName, Class<?> serviceClass) {
        synchronized (lock) {
            Object service = cache.get(serviceClass.getName());
            
            if (service == null) {
                final Properties properties = new Properties();

                properties.setProperty("CRS_SERVICE_URL", serviceUrl);
                properties.setProperty("CRS_SERVICE_NAME", serviceName);
                properties.setProperty("CRS_SERVICE_CLASS", serviceClass.getName());

                service = new ClassPathXmlApplicationContext(CLIENT_CONTEXT_FILE) {
                    @Override
                    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                        super.prepareBeanFactory(beanFactory);
                        ((PropertyPlaceholderConfigurer) beanFactory.getBean("properties")).setProperties(properties);
                    }
                }.getBean("crsService");
                
                cache.put(serviceClass.getName(), service);
            }
            
            return service;
        }
    }
}
