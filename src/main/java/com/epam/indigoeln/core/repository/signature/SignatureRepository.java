package com.epam.indigoeln.core.repository.signature;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Repository
public class SignatureRepository {

    private static final String SESSION_ID_ATTRIBUTE = "SignatureSessionId";

    @Value("${integration.signatureservice.url}")
    private String signatureServiceUrl;

    @Value("${integration.signatureservice.username}")
    private String username;

    @Value("${integration.signatureservice.password}")
    private String password;

    private RestTemplate restTemplate = new RestTemplate();

    public String getSignatureTemplates(String username) {
        return exchange(signatureServiceUrl + "/api/getTemplates?username={username}", HttpMethod.GET, null,
                String.class, Collections.singletonMap("username", username)).getBody();
    }

    public String uploadDocument(String username, String templateId, final String fileName, byte[] file) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(templateId)) {
            return StringUtils.EMPTY;
        }

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("templateId", templateId);
        ByteArrayResource fileResource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        map.add("file", fileResource);

        return exchange(signatureServiceUrl + "/api/uploadDocument", HttpMethod.POST, map,
                String.class, new HashMap<>()).getBody();
    }

    private <E> ResponseEntity<E> exchange(String url, HttpMethod method, Object body, Class<E> clazz,
                                           Map<String, Object> args) {
        String sessionId = (String) RequestContextHolder.getRequestAttributes().getAttribute(SESSION_ID_ATTRIBUTE,
                RequestAttributes.SCOPE_SESSION);
        HttpEntity<Object> entity = new HttpEntity<>(body, header(HttpHeaders.COOKIE, "JSESSIONID=" + sessionId));
        try {
            return restTemplate.exchange(url, method, entity, clazz, args);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                sessionId = login(username, password);
                RequestContextHolder.getRequestAttributes().setAttribute(SESSION_ID_ATTRIBUTE, sessionId,
                        RequestAttributes.SCOPE_SESSION);
                entity = new HttpEntity<>(body, header(HttpHeaders.COOKIE, "JSESSIONID=" + sessionId));
                return restTemplate.exchange(url, method, entity, clazz, args);
            } else {
                throw e;
            }
        }
    }

    private HttpHeaders header(String name, String value) {
        HttpHeaders result = new HttpHeaders();
        result.add(name, value);
        return result;
    }

    private String login(String username, String password) {
        Map<String, Object> o = new HashMap<>();
        o.put("username", username);
        o.put("password", password);
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(signatureServiceUrl + "/loginProcess", o,
                Object.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            HttpHeaders headers = responseEntity.getHeaders();
            if (headers.containsKey(HttpHeaders.SET_COOKIE)) {
                String cookieHeader = headers.get(HttpHeaders.SET_COOKIE).get(0);
                String[] splitted = cookieHeader.split(";");

                for (String s : splitted) {
                    String[] map = s.split(",");
                    for (String m : map) {
                        if (m.contains("JSESSIONID")) {
                            return m.split("=")[1];
                        }
                    }
                }
            }
        }
        return null;
    }


}
