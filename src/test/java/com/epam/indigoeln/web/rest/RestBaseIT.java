package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.util.AuthUtil;
import com.epam.indigoeln.util.DatabaseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class RestBaseIT {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected DatabaseUtil databaseUtil;

    protected String[] cookie;
    protected String csrfToken;

    @Before
    public void setUp() throws Exception {
        ClientHttpRequestFactory requestFactory = restTemplate.getRestTemplate().getRequestFactory();
        if (requestFactory instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory) requestFactory).setOutputStreaming(false);
        }

        AuthUtil authUtil = new AuthUtil(restTemplate);
        cookie = authUtil.getCookie();
        csrfToken = authUtil.getCsrfToken();
        databaseUtil.init();
    }

    @After
    public void tearDown(){
        databaseUtil.dropDBs();
    }
}
