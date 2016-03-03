package com.epam.indigoeln.core.repository.registration.crs;

import com.epam.indigo.crs.exceptions.CRSException;
import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CrsRegistrationRepository implements RegistrationRepository {

    private static final String ID = "CRS";
    private static final String NAME = "CRS Service";
    private static RegistrationRepositoryInfo INFO = new RegistrationRepositoryInfo(ID, NAME);

    @Value("${crs.service.username}")
    private String username;

    @Value("${crs.service.password}")
    private String password;

    @Autowired
    private BingoRegistration registration;

    @Autowired
    private BingoSearch search;

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        registration = getService(BingoRegistration.class);
//        search = getService(BingoSearch.class);
//    }

//    private <T> T getService(Class<T> clazz) {
//        HttpInvokerClientInterceptor interceptor = new HttpInvokerClientInterceptor();
//        interceptor.setServiceUrl(url);
//        return (T) new ProxyFactory(clazz, interceptor).getProxy(beanClassLoader);
//    }

    @Override
    public List<Integer> searchSim(String structure, String similarity, Double var3, Double var4) throws RegistrationException {
        try {
            return search.searchSim(structure, similarity, var3, var4);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchSmarts(String structure) throws RegistrationException {
        try {
            return search.searchSmarts(structure);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchSub(String structure, String searchOption) throws RegistrationException {
        try {
            return search.searchSub(structure, searchOption);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchExact(String structure, String searchOption) throws RegistrationException {
        try {
            return search.searchExact(structure, searchOption);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public RegistrationRepositoryInfo getInfo() {
        return INFO;
    }

}
