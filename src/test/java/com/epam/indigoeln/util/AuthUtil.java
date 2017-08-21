package com.epam.indigoeln.util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN;
import static org.junit.Assert.assertEquals;

public class AuthUtil {
    public static final String login = "admin";
    public static final String password = "admin";
    private String[] cookie;
    private String csrfToken;

    public AuthUtil(TestRestTemplate restTemplate) throws URISyntaxException {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("j_username", login);
        params.add("j_password", password);

        RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = RequestEntity.post(new URI("/api/authentication")).body(params);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        csrfToken = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .map(c -> c.split(";")[0].split("="))
                .filter(c -> c[0].equals(CSRF_TOKEN))
                .map(c -> c[1])
                .findAny().orElse("");

        List<String> cookie = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
        this.cookie = cookie.toArray(new String[cookie.size()]);
    }

    public String[] getCookie() {
        return cookie;
    }

    public String getCsrfToken() {
        return csrfToken;
    }
}
