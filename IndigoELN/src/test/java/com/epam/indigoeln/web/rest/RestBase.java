package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.util.AuthUtil;
import com.epam.indigoeln.util.DatabaseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class RestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected DatabaseUtil databaseUtil;

    protected String[] cookie;
    protected String csrfToken;

    @Before
    public void setUp() throws Exception {
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
