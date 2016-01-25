package com.epam.indigoeln.core.integration;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestClient {

    private RestClient() {
    }

    public static <Response> Response GET(String url, HttpHeaders headers, Class responseClass) {
        return execute(url, headers, HttpMethod.GET, null, responseClass);
    }

    public static <Response> Response DELETE(String url, HttpHeaders headers, Class responseClass) {
        return execute(url, headers, HttpMethod.DELETE, null, responseClass);
    }

    public static <Response> Response PUT(String url, HttpHeaders headers, Object requestBody, Class responseClass) {
        return execute(url, headers, HttpMethod.PUT, requestBody, responseClass);
    }

    public static <Response> Response POST(String url, HttpHeaders headers, Object requestBody, Class responseClass) {
        return execute(url, headers, HttpMethod.POST, requestBody, responseClass);
    }

    public static HttpHeaders basicAuthorization(String login, String password) {
        HttpHeaders headers = new HttpHeaders();
        String base64Credentials = new String(Base64.encodeBase64(String.format("%s:%s", login, password).getBytes()));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private static <Response> Response execute(String url, HttpHeaders headers, HttpMethod method,
                                               Object requestBody, Class responseClass){
        HttpEntity httpRequest = new HttpEntity(requestBody, headers);
        ResponseEntity httpResponse = new RestTemplate().exchange(url, method, httpRequest, responseClass);
        return (Response) httpResponse.getBody();
    }
}
